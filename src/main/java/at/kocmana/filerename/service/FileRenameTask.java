package at.kocmana.filerename.service;

import at.kocmana.filerename.model.CommandLineArguments;
import at.kocmana.filerename.model.JobArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRenameTask implements Callable<Boolean> {

  private static final Logger log = LoggerFactory.getLogger(FileRenameTask.class);

  private static final String FILENAME_REGEX_STRING =
          "(?<leading>.*?)(<<(?<date>.*)>>)(?<trailing>.*?).(?<fileSuffix>.+)\\b";
  private static final Pattern FILENAME_PATTERN = Pattern.compile(FILENAME_REGEX_STRING);

  private CommandLineArguments arguments;

  private TaskStatus taskStatus = TaskStatus.CREATED;
  private List<FileRenameJob> fileRenameJobs = Collections.emptyList();

  private DateTimeFormatter dtfIn;
  private DateTimeFormatter dtfOut;

  public FileRenameTask(CommandLineArguments arguments) {
    this.arguments = arguments;
  }

  public TaskStatus getTaskStatus() {
    return taskStatus;
  }

  @Override
  public Boolean call() throws Exception {

    extractDateFormatFromTemplates();
    generateRenameJobs();


//
//      relevantFiles
//              .map(path -> path.getFileName() + "->" +
//                      transformFilename(path.getFileName().toString(), extractDate(path.getFileName().toString(), filePattern),
//                              arguments.outputTemplate(), dtfIn, dtfOut))
//              //.map(path -> extractDate(path.getFileName().toString(), filePattern))
//              .forEach(System.out::println);
//    }
//    System.out.println(arguments.path().normalize().
//            toAbsolutePath());

    return true;
  }

  private void extractDateFormatFromTemplates() {

    Matcher inputMatcher = FILENAME_PATTERN.matcher(arguments.inputTemplate());
    Matcher outputMatcher = FILENAME_PATTERN.matcher(arguments.outputTemplate());

    if (!inputMatcher.find()) {
      failTask("Input Pattern did not comply to required pattern: {}", FILENAME_REGEX_STRING);
    }
    log.debug("Input Date: {}", inputMatcher.group("date"));
    if (!outputMatcher.find()) {
      failTask("Output Pattern did not comply to required pattern: {}", FILENAME_REGEX_STRING);
    }
    log.debug("Output Date: {}", outputMatcher.group("date"));

    if (inputMatcher.group("date").isBlank()) {
      failTask("Could not identify date to parse.");
    }
    dtfIn = DateTimeFormatter.ofPattern(inputMatcher.group("date"));
    dtfOut = DateTimeFormatter.ofPattern(outputMatcher.group("date"));
  }

  private void generateRenameJobs() {
    var filePatternString = arguments.inputTemplate().replaceAll("<<.*>>", "(?<date>.*?)");

    var filePattern = Pattern.compile(filePatternString);
    var searchPattern = filePattern.asPredicate();

    BiPredicate<Path, BasicFileAttributes> searchCriteria = (path, attributes) -> {
      var filename = path.getFileName().toString();
      return attributes.isRegularFile() && searchPattern.test(filename);
    };

    try (var relevantFiles = Files.find(arguments.path(), Integer.MAX_VALUE, searchCriteria)) {
      fileRenameJobs = relevantFiles
              .map(file -> new JobArguments(file, dtfIn, dtfOut, arguments.outputTemplate()))
              .map(FileRenameJob::new)
              .toList();
    } catch (IOException exception) {
      failTask("Could not lookup files in directory {}: {}",
             arguments.path().toAbsolutePath().toString(),
              exception.getMessage());
    }
  }

  private static String extractDate(String fileName, Pattern filePattern) {
    System.out.println("Assessing " + fileName);

    var match = filePattern.matcher(fileName);
    if (!match.find()) {
      System.out.println("Not found");
      return "NOT Treated";
    }
    return match.group("date");
  }

  private static String transformFilename(String filename, String dateTime, String outputPattern,
                                          DateTimeFormatter dtfIn, DateTimeFormatter dtfOut) {
    var dateTimeParsed = dtfIn.parse(dateTime);

    return outputPattern.replaceAll("<<.*?>>", dtfOut.format(dateTimeParsed));
  }

  private void failTask(String errorMessage, Object... errorMessageArguments) {
    log.error(errorMessage, errorMessageArguments);
    taskStatus = TaskStatus.FAILURE;
  }

  private enum TaskStatus {
    CREATED, READY, RUNNING, SUCCESS, FAILURE
  }

}
