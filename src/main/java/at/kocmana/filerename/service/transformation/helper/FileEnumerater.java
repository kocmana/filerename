package at.kocmana.filerename.service.transformation.helper;

import static java.util.Objects.isNull;

import at.kocmana.filerename.model.Region;
import java.util.regex.Pattern;

public class FileEnumerater {

  private static final String DOT_GROUP_NAME = "dot";
  private static final String FILENAME_REGEX = "(?<filename>.+)(?<" + DOT_GROUP_NAME + ">\\.)(?<fileSuffix>.+)";
  private static final Pattern FILENAME_PATTERN = Pattern.compile(FILENAME_REGEX);

  private final String filename;
  private final Region regionToReplace;
  private int currentNumber = 1;

  public static FileEnumerater forFilename(String filename) {
    if (isNull(filename) || filename.isBlank()) {
      throw new IllegalArgumentException("Filename is null or blank");
    }
    return new FileEnumerater(filename);
  }

  private FileEnumerater(String filename) {
    this.filename = filename;
    this.regionToReplace = determineRegionToReplace();
  }

  private Region determineRegionToReplace() {
    var matcher = FILENAME_PATTERN.matcher(filename);
    matcher.find();
    return new Region(matcher.start(DOT_GROUP_NAME), matcher.end(DOT_GROUP_NAME));
  }

  public String enumerateFilename() {
    var stringBuilder = new StringBuilder(filename);
    stringBuilder.replace(regionToReplace.offsetFrom(), regionToReplace.offsetTo(), getFileSuffix());
    return stringBuilder.toString();
  }

  private String getFileSuffix() {
    return String.format("-%d.", currentNumber++);
  }

  public String getFilename() {
    return filename;
  }

  public int getCurrentNumber() {
    return currentNumber;
  }
}
