package at.kocmana.filerename;

import at.kocmana.filerename.model.Arguments;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileRenameTask implements Callable<Boolean> {

  private Arguments arguments;

  public FileRenameTask(Arguments arguments) {
    this.arguments = arguments;
  }

  @Override
  public Boolean call() throws Exception {

    var currentPath = Paths.get(".");

    //var inputPattern = "IMG_<<yyyyMMdd_HHmmss>>.jpg";
    //var outputPattern = "<<yyyyMMdd_HHmmss>>.jpg";
    var pattern = Pattern.compile("(?<leading>.*?)(<<(?<date>.*)>>)(?<trailing>.*?).(?<fileSuffix>.+)\\b");

    Matcher inputMatcher = pattern.matcher(arguments.inputTemplate());
    Matcher outputMatcher = pattern.matcher(arguments.outputTemplate());

    if (!inputMatcher.find() || !outputMatcher.find()) {
      return false;
    }

    System.out.println(inputMatcher.group("date"));
    System.out.println(outputMatcher.group("date"));

    if (inputMatcher.group("date").isBlank()) {
      return false;
    }
    var dtfIn = DateTimeFormatter.ofPattern(inputMatcher.group("date"));
    var dtfOut = DateTimeFormatter.ofPattern(outputMatcher.group("date"));
    var filePatternString = arguments.inputTemplate().replaceAll("<<.*>>", "(?<date>.*?)");

    System.out.println("FilePattern String: " + filePatternString);
    var filePattern = Pattern.compile(filePatternString);
    var filenamePredicate = filePattern.asPredicate();

    BiPredicate<Path, BasicFileAttributes> searchCriteria = (path, attributes) -> {
      var filename = path.getFileName().toString();
      //System.out.println(filename + ": " + filenamePredicate.test(filename));
      return attributes.isRegularFile() && filenamePredicate.test(filename);
    };

    try (
        Stream<Path> relevantFiles = Files.find(currentPath, Integer.MAX_VALUE, searchCriteria)) {
      relevantFiles
          .map(path -> path.getFileName() + "->" +
              transformFilename(path.getFileName().toString(), extractDate(path.getFileName().toString(), filePattern),
                  arguments.outputTemplate(), dtfIn, dtfOut))
          //.map(path -> extractDate(path.getFileName().toString(), filePattern))
          .forEach(System.out::println);
    }
    System.out.println(currentPath.normalize().
        toAbsolutePath());

    return true;
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

}
