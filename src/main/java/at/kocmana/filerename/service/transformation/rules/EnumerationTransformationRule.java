package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.service.transformation.TransformationRuleGenerator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumerationTransformationRule extends AbstractTransformationRule {

  private static final Logger log = LoggerFactory.getLogger(EnumerationTransformationRule.class);

  private static final String RULE_ABBREVIATION = "E";
  private static final String DEFAULT_ENUMERATION_FORMAT = "%d";

  private final String enumerationFormat;
  private final AtomicInteger currentIndex = new AtomicInteger(0);

  public static final TransformationRuleGenerator FACTORY_METHOD =
      EnumerationTransformationRule::generateIfRuleIsApplicable;

  private static Optional<TransformationRule> generateIfRuleIsApplicable(String inputPattern,
                                                                         String outputPattern) {
    if (inputPattern == null || outputPattern == null) {
      return Optional.empty();
    }

    if (!ruleMarkerIsPresent(RULE_ABBREVIATION, outputPattern)) {
      return Optional.empty();
    }

    return Optional.of(new EnumerationTransformationRule(inputPattern, outputPattern));
  }

  public EnumerationTransformationRule(String inputPattern, String outputPattern) {
    super(RULE_ABBREVIATION, inputPattern, outputPattern);
    var outputRuleArguments = this.getOutputRuleArguments();
    enumerationFormat = extractNumberFormat(outputRuleArguments);
  }

  public String toString() {
    var exampleNumber = 12;
    var formattedExampleNumber = String.format(enumerationFormat, exampleNumber);
    return String.format(
        "Enumeration Transformation Rule: Adding enumeration with pattern \"%s\": \"%d\" will be displayed as \"%s\".",
        enumerationFormat, exampleNumber, formattedExampleNumber);
  }

  private static String extractNumberFormat(String numberFormat) {
    if (numberFormat == null || numberFormat.isBlank()) {
      return DEFAULT_ENUMERATION_FORMAT;
    }
    validateNumberFormat(numberFormat);
    return numberFormat;
  }

  private static void validateNumberFormat(String numberFormat) {
    try {
      var testOutput = String.format(numberFormat, 1);
    } catch (Exception exception) {
      var message = String.format("Enumeration format \"%s\" does not seem to be a valid format: %s",
          numberFormat, exception.getMessage());
      throw new IllegalArgumentException(message);
    }
  }

  @Override
  public String replaceTemplateWithSearchString(String inputPattern) {
    //since this transformation rule only affects the output pattern, just return the original input pattern.
    return inputPattern;
  }

  @Override
  public String apply(String filename, String outputPattern) {
    var formattedIndex = String.format(enumerationFormat, this.currentIndex.getAndIncrement());
    return outputPattern.replaceAll(getGenericRulePattern(), formattedIndex);
  }
}
