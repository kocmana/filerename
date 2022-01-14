package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.service.transformation.TransformationRuleGenerator;
import java.util.Optional;
import java.util.regex.Pattern;

public class RegexTransformationRule extends AbstractTransformationRule {

  private static final String RULE_ABBREVIATION = "R";

  private final String regexGroup;
  private final Pattern regexGroupPattern;

  public static final TransformationRuleGenerator FACTORY_METHOD =
      RegexTransformationRule::generateIfRuleIsApplicable;

  private static Optional<TransformationRule> generateIfRuleIsApplicable(String inputPattern, String outputPattern) {
    if (!ruleMarkerIsPresent(RULE_ABBREVIATION, inputPattern)) {
      return Optional.empty();
    }

    return Optional.of(new RegexTransformationRule(inputPattern, outputPattern));
  }

  public RegexTransformationRule(String inputFilenamePattern,
                                 String outputFilenamePattern) {
    super(RULE_ABBREVIATION, inputFilenamePattern, outputFilenamePattern);

    this.regexGroup = createRegexGroup();
    this.regexGroupPattern = Pattern.compile(regexGroup);
  }

  @Override
  public String toString() {
    return String.format("RegexTransformationRule: Will match instances of \"%s\" in the input pattern.",
        getInputRuleArguments());
  }

  @Override
  public String replaceTemplateWithSearchString(String pattern) {
    return pattern.replaceAll(getGenericRulePattern(), regexGroup);
  }

  private String createRegexGroup() {
    return String.format("(?<%s>%s)",
        RULE_ABBREVIATION, getInputRuleArguments());
  }

  @Override
  protected String apply(String filename, String outputPattern) {
    var inputMatcher = matcherFor(regexGroupPattern, filename)
        .orElseThrow(() -> (new IllegalStateException(
            String.format("Could not find %s in filename %s despite it matched.", regexGroup, filename))));
    var inputString = inputMatcher.group(RULE_ABBREVIATION);
    return outputPattern.replaceAll(getGenericRulePattern(), inputString);
  }
}
