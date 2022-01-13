package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.service.transformation.TransformationRuleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TimestampTransformationRule extends AbstractTransformationRule {

  private static final Logger log = LoggerFactory.getLogger(TimestampTransformationRule.class);

  private static final String RULE_ABBREVIATION = "TS";

  private final DateTimeFormatter dtfIn;
  private final DateTimeFormatter dtfOut;

  public static final TransformationRuleGenerator FACTORY_METHOD = TimestampTransformationRule::generateIfRuleIsApplicable;

  private static Optional<TransformationRule> generateIfRuleIsApplicable(String inputPattern,
                                                                         String outputPattern) {
    if (inputPattern == null || outputPattern == null) {
      return Optional.empty();
    }

    if (!ruleMarkerIsPresent(RULE_ABBREVIATION, inputPattern) || !ruleMarkerIsPresent(RULE_ABBREVIATION, outputPattern)) {
      return Optional.empty();
    }

    return Optional.of(new TimestampTransformationRule(inputPattern, outputPattern));
  }

  private TimestampTransformationRule(String inputPattern, String outputPattern) {
    super(RULE_ABBREVIATION, inputPattern, outputPattern);
    if (getInputRuleArguments() == null || getInputRuleArguments().isBlank()) {
      throw new IllegalArgumentException("No input timestamp pattern provided for timestamp transformation rule " +
              "(eg. <<TS|yyyy-MM-dd>>)");
    }
    if (ruleMarkerIsPresent(RULE_ABBREVIATION, outputPattern) &&
            (getOutputRuleArguments() == null || getOutputRuleArguments().isBlank())) {
      throw new IllegalArgumentException("No output timestamp pattern provided for timestamp transformation rule " +
              "(eg. <<TS|yyyy-MM-dd>>)");
    }

    this.dtfIn = DateTimeFormatter.ofPattern(this.getInputRuleArguments());
    this.dtfOut = DateTimeFormatter.ofPattern(this.getOutputRuleArguments());
  }

  public String toString() {
    var now = LocalDateTime.now();
    return String.format("Timestamp Transformation Rule: Replacing timestamp pattern %s (e.g. \"%s\") with \"%s\" (e.g. %s).",
            getInputRuleArguments(), dtfIn.format(now), getOutputRuleArguments(), dtfOut.format(now));
  }

  @Override
  public String apply(String filename, String outputPattern) {
    var inputMatcher = fileNameMatcherFor(filename)
            .orElseThrow(() -> new IllegalArgumentException(String.format("Could not identify date pattern in filename \"%s\"", filename)));

    var inputDate = dtfIn.parse(inputMatcher.group(RULE_ABBREVIATION));
    var outputDate = dtfOut.format(inputDate);
    return outputPattern.replaceAll(getGenericRulePattern(), outputDate);
  }

  public DateTimeFormatter getDtfIn() {
    return dtfIn;
  }

  public DateTimeFormatter getDtfOut() {
    return dtfOut;
  }
}
