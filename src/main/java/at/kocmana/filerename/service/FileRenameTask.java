package at.kocmana.filerename.service;

import at.kocmana.filerename.model.CommandLineArguments;
import at.kocmana.filerename.model.JobArguments;
import at.kocmana.filerename.service.transformation.TransformationRule;
import at.kocmana.filerename.service.transformation.TransformationRuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileRenameTask implements Callable<Boolean> {

  private static final Logger log = LoggerFactory.getLogger(FileRenameTask.class);
  private final CommandLineArguments arguments;

  private TaskStatus taskStatus = TaskStatus.CREATED;
  private List<FileRenameJob> fileRenameJobs = Collections.emptyList();
  private List<TransformationRule> transformationRules = Collections.emptyList();

  public FileRenameTask(CommandLineArguments arguments) {
    this.arguments = arguments;
  }

  public TaskStatus getTaskStatus() {
    return taskStatus;
  }

  @Override
  public Boolean call() {
    transformationRules = TransformationRuleFactory.generateApplicableTransformationRules(arguments.inputTemplate(), arguments.outputTemplate());
    var searchString = generateFileSearchPattern();
    generateRenameJobs(searchString);

    this.taskStatus = TaskStatus.RUNNING;

    log.info(fileRenameJobs.stream()
            .map(Objects::toString)
            .collect(Collectors.joining(",")));

    fileRenameJobs.parallelStream()
            .forEach(FileRenameJob::call);

    this.taskStatus = TaskStatus.SUCCESS;

    return true;
  }

  public String getStatus() {
    return fileRenameJobs.stream()
            .map(Objects::toString)
            .collect(Collectors.joining("\r\n"));
  }

  private String generateFileSearchPattern() {
    var searchString = arguments.inputTemplate();
    for (var transformationRule : transformationRules) {
      searchString = transformationRule.replaceTemplateWithSearchString(searchString);
    }
    return searchString;
  }

  private void generateRenameJobs(String searchString) {
    var filePattern = Pattern.compile(searchString);
    var searchPattern = filePattern.asPredicate();

    BiPredicate<Path, BasicFileAttributes> searchCriteria = (path, attributes) -> {
      var filename = path.getFileName().toString();
      return attributes.isRegularFile() && searchPattern.test(filename);
    };

    try (var relevantFiles = Files.find(arguments.path(), Integer.MAX_VALUE, searchCriteria)) {
      fileRenameJobs = relevantFiles
              .map(file -> new JobArguments(file, transformationRules, arguments.outputTemplate(), arguments.dryRun()))
              .map(FileRenameJob::new)
              .toList();
      log.info("Relevant files: {}", relevantFiles);
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
    CREATED, RUNNING, SUCCESS, FAILURE
  }

}
