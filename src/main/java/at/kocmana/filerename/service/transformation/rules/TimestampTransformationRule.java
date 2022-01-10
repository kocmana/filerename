package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.service.transformation.TransformationRuleGenerator;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampTransformationRule implements TransformationRule {
  //(?<leading>.*?)(<{2}TS(\|(?<dateFormat>.+))?>{2})(?<trailing>.*?)\.(?<fileSuffix>.+)\b
  private static final String DATE_FORMAT_EXTRACTION_TEMPLATE = "(?<leading>.*?)(<{2}TS(\\|(?<dateFormat>.+))?>{2})(?<trailing>.*?)\\.(?<fileSuffix>.+)\\b";
  private static final Pattern DATE_FORMAT_EXTRACTION_PATTERN = Pattern.compile(DATE_FORMAT_EXTRACTION_TEMPLATE);

  private static final String TIMESTAMP_TRANSFORMATION_RULE_MARKER = "(<{2}TS\\|.*?>{2})";
  private static final String DATE_GROUP_MARKER = "(?<date>.*?)";

  private final Pattern inputSearchPattern;
  private final Pattern outputSearchPattern;
  private final TransformationRuleIdentity identity;
  private final DateTimeFormatter dtfIn;
  private final DateTimeFormatter dtfOut;

  private TimestampTransformationRule(Pattern inputSearchPattern, Pattern outputSearchPattern,
                                      DateTimeFormatter dtfIn, DateTimeFormatter dtfOut,
                                      TransformationRuleIdentity identity) {
    this.inputSearchPattern = inputSearchPattern;
    this.outputSearchPattern = outputSearchPattern;
    this.dtfIn = dtfIn;
    this.dtfOut = dtfOut;
    this.identity = identity;
  }

  public String toString() {
    var now = LocalDateTime.now();
    return String.format("Timestamp Transformation Rule: Replacing timestamp pattern \"%s\" with \"%s\".",
            dtfIn.format(now), dtfOut.format(now));
  }

  public static final TransformationRuleGenerator FACTORY_METHOD = TimestampTransformationRule::generateIfRuleIsApplicable;

  private static Optional<TransformationRule> generateIfRuleIsApplicable(String inputPattern,
                                                                         String outputPattern) {
    if (inputPattern == null || outputPattern == null) {
      return Optional.empty();
    }

    var inputDateFormatMatcher = DATE_FORMAT_EXTRACTION_PATTERN.matcher(inputPattern);
    if (!inputDateFormatMatcher.find()) {
      return Optional.empty();
    }
    var outputDateFormatMatcher = DATE_FORMAT_EXTRACTION_PATTERN.matcher(outputPattern);
    if (!outputDateFormatMatcher.find()) {
      throw new IllegalArgumentException("Date template found in source pattern but not target pattern.");
    }
    var dtfIn = generateDtf(inputDateFormatMatcher);
    var dtfOut = generateDtf(outputDateFormatMatcher);
    var identity = new TransformationRuleIdentity(inputDateFormatMatcher.start(), inputDateFormatMatcher.end());

    var inputSearchPattern = transformCliArgumentPatternToFilePattern(inputPattern);
    var outputSearchPattern = transformCliArgumentPatternToFilePattern(outputPattern);

    return Optional.of(new TimestampTransformationRule(inputSearchPattern, outputSearchPattern, dtfIn, dtfOut, identity));
  }

  private static DateTimeFormatter generateDtf(Matcher matcher) {
    String dateTimeFormat = matcher.group("dateFormat");
    return DateTimeFormatter.ofPattern(dateTimeFormat);
  }

  private static Pattern transformCliArgumentPatternToFilePattern(String cliArgumentPattern) {
    var inputSearchString = toSearchString(cliArgumentPattern);
    return Pattern.compile(inputSearchString);
  }

  private static String toSearchString(String argumentPattern) {
    return argumentPattern.replaceAll(TIMESTAMP_TRANSFORMATION_RULE_MARKER, DATE_GROUP_MARKER);
  }

  @Override
  public String replaceTemplateWithSearchString(String inputPattern) {
    return toSearchString(inputPattern);
  }

  @Override
  public TransformationRuleIdentity getIdentity() {
    return identity;
  }

  @Override
  public String apply(Path file, String outputPattern) {
    var filename = file.getFileName().toString();
    var inputMatcher = inputSearchPattern.matcher(filename);
    if (!inputMatcher.find()) {
      var message = String.format("Could not identify date pattern in filename \"%s\"", filename);
      throw new IllegalStateException(message);
    }
    var inputDate = dtfIn.parse(inputMatcher.group("date"));
    var outputDate = dtfOut.format(inputDate);
    return outputPattern.replaceAll(TIMESTAMP_TRANSFORMATION_RULE_MARKER, outputDate);
  }


  public DateTimeFormatter getDtfIn() {
    return dtfIn;
  }

  public DateTimeFormatter getDtfOut() {
    return dtfOut;
  }
}
