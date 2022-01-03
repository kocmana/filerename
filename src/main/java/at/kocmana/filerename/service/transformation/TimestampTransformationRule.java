package at.kocmana.filerename.service.transformation;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampTransformationRule implements TransformationRule {

  private static final Pattern TRANSFORMATION_RULE_PATTERN = Pattern.compile("<<(?<date>.*)>>");
  private final Matcher outputMatcher;
  private final TransformationRuleIdentity identity;
  private final DateTimeFormatter dtfIn;
  private final DateTimeFormatter dtfOut;

  private TimestampTransformationRule(DateTimeFormatter dtfIn, DateTimeFormatter dtfOut, Matcher outputMatcher,
                                      TransformationRuleIdentity identity) {
    this.dtfIn = dtfIn;
    this.dtfOut = dtfOut;
    this.outputMatcher = outputMatcher;
    this.identity = identity;
  }

  public static Optional<TimestampTransformationRule> generateIfRuleIsApplicable(String inputPattern,
                                                                                 String outputPattern) {
    if(inputPattern == null || outputPattern == null) {
      return Optional.empty();
    }

    var inputMatcher = TRANSFORMATION_RULE_PATTERN.matcher(inputPattern);
    if (!inputMatcher.find()) {
      return Optional.empty();
    }
    var outputMatcher = TRANSFORMATION_RULE_PATTERN.matcher(outputPattern);
    if (!outputMatcher.find()) {
      throw new IllegalArgumentException("Date template found in source pattern but not target pattern.");
    }
    var identity = new TransformationRuleIdentity(inputMatcher.start(), inputMatcher.end());
    return Optional.of(
        new TimestampTransformationRule(generateDtf(inputMatcher), generateDtf(outputMatcher), outputMatcher, identity));
  }

  private static DateTimeFormatter generateDtf(Matcher matcher) {
    String dateTimeFormat = matcher.group("date");
    return DateTimeFormatter.ofPattern(dateTimeFormat);
  }

  @Override
  public TransformationRuleIdentity getIdentity() {
    return identity;
  }

  @Override
  public void prepare() {

  }

  @Override
  public String apply(String filename) {
    outputMatcher.replaceFirst("TODO");
    return null;
  }

  public DateTimeFormatter getDtfIn() {
    return dtfIn;
  }

  public DateTimeFormatter getDtfOut() {
    return dtfOut;
  }
}
