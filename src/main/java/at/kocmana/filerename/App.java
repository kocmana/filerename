package at.kocmana.filerename;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Hello world!
 */
public class App {
  public static void main(String[] args) throws IOException {
    var currentPath = Paths.get(".");

    var inputPattern = "IMG_<<yyyyMMdd_HHmmss>>.jpg";
    var outputPattern = "<<yyyyMMdd_HHmmss>>.jpg";
    var pattern = Pattern.compile("(?<leading>.*?)(<<(?<date>.*)>>)(?<trailing>.*?).(?<fileSuffix>.+)\\b");

    Matcher inputMatcher = pattern.matcher(inputPattern);
    Matcher outputMatcher = pattern.matcher(outputPattern);

    if (!inputMatcher.find() || !outputMatcher.find()) {
      return;
    }

    var groupCount = inputMatcher.groupCount();
    System.out.println(inputMatcher.group("date"));
    System.out.println(outputMatcher.group("date"));

    if (inputMatcher.group("date").isBlank()) {
      return;
    }
    var dtfIn = DateTimeFormatter.ofPattern(inputMatcher.group("date"));
    var dtfOut = DateTimeFormatter.ofPattern(outputMatcher.group("date"));
    var filePatternString = inputPattern.replaceAll("<<.*>>", "(?<date>.*?)");

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
                  outputPattern, dtfIn, dtfOut))
          //.map(path -> extractDate(path.getFileName().toString(), filePattern))
          .forEach(System.out::println);
    }
    System.out.println(currentPath.normalize().

        toAbsolutePath());
  }

  private static String extractDate(String fileName, Pattern filePattern) {
    System.out.println("Assessing " + fileName);

    var match = filePattern.matcher(fileName);
    if(!match.find()){
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
