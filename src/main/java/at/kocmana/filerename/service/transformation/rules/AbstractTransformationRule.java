package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.model.exception.MethodNotImplementedException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTransformationRule implements TransformationRule {

  private static final String RULE_EXTRACTION_LEADING_TEMPLATE = "(?<leading>.*?)";
  private static final String RULE_EXTRACTION_TRAILING_TEMPLATE = "(?<trailing>.*?)\\.(?<fileSuffix>.+)\\b";
  private static final String RULE_START_MARKER = "(?<rule>\\<{2}";
  private static final String RULE_ARGUMENTS_MARKER = "(\\|(?<ruleArguments>.+))?";
  private static final String RULE_END_MARKER = "\\>{2})";

  private final String ruleExtractionTemplate;
  private final Pattern ruleExtractionPattern;

  private final String ruleAbbreviation;
  private final String genericRulePattern;
  private final String inputRuleArguments;
  private final String outputRuleArguments;
  private final String filenameRuleGroupName;

  private final String inputFilenamePattern;
  private final String outputFilenamePattern;

  private final TransformationRuleIdentity identity;

  protected AbstractTransformationRule(String ruleAbbreviation, String inputFilenamePattern, String outputFilenamePattern) {
    this.ruleAbbreviation = ruleAbbreviation;
    this.inputFilenamePattern = inputFilenamePattern;
    this.outputFilenamePattern = outputFilenamePattern;
    ruleExtractionTemplate = generateTemplateExtractionPattern(ruleAbbreviation);
    ruleExtractionPattern = Pattern.compile(ruleExtractionTemplate);
    genericRulePattern = generateGroupPattern(ruleAbbreviation);
    filenameRuleGroupName = generateFilenameGroupPattern(ruleAbbreviation);
    inputRuleArguments = extractGroup(inputFilenamePattern);
    outputRuleArguments = extractGroup(outputFilenamePattern);
    identity = generateIdentity();
  }

  private String generateGroupPattern(String ruleShortcut) {
    return String.format("\\<{2}%s(|.*)?\\>{2}", ruleShortcut);
  }

  private String generateFilenameGroupPattern(String ruleAbbreviation) {
    return String.format("<<%s>>", ruleAbbreviation);
  }

  private String extractGroup(String filenamePattern) {
    return matcherFor(filenamePattern)
        .map(m -> m.group("ruleArguments"))
        .orElse("");
  }

  private TransformationRuleIdentity generateIdentity() {
    var matcher = matcherFor(outputFilenamePattern)
        .orElseThrow(() -> generateIllegalStateException("Could not create rule identity for rule %s pattern \"%s\"",
            this.getClass().getSimpleName(), outputFilenamePattern));
    return new TransformationRuleIdentity(matcher.start("rule"), matcher.end("rule"));
  }

  protected IllegalStateException generateIllegalStateException(String messageTemplate, String... arguments) {
    var message = String.format(messageTemplate, arguments);
    return new IllegalStateException(message);
  }

  private Optional<Matcher> matcherFor(String filenamePattern) {
    if (filenamePattern == null) {
      throw new IllegalArgumentException("Can't extract matcher. Provided filename is null");
    }
    var matcher = ruleExtractionPattern.matcher(filenamePattern);
    if (!matcher.find()) {
      return Optional.empty();
    }
    return Optional.of(matcher);
  }

  private static String generateTemplateExtractionPattern(String templateShortcut) {
    //(?<leading>.*?)(<{2}TS(\|(?<dateFormat>.+))?>{2})(?<trailing>.*?)\.(?<fileSuffix>.+)\b
    //(?<leading>.*?)(?<rule>\<{2}TS(\|(?<ruleArguments>.+))?\>{2})(?<trailing>.*?)\.(?<fileSuffix>.+)\b
    return RULE_EXTRACTION_LEADING_TEMPLATE
        + RULE_START_MARKER
        + templateShortcut
        + RULE_ARGUMENTS_MARKER
        + RULE_END_MARKER
        + RULE_EXTRACTION_TRAILING_TEMPLATE;
  }

  protected static boolean ruleMarkerIsPresent(String ruleShortcut, String filenamePattern) {
    if (filenamePattern == null || filenamePattern.isBlank()){
      return false;
    }
    var ruleExtractionPattern = Pattern.compile(generateTemplateExtractionPattern(ruleShortcut));
    var matcher = ruleExtractionPattern.matcher(filenamePattern);
    return matcher.find();
  }

  @Override
  public String replaceTemplateWithSearchString(String pattern) {
    //return pattern.replaceAll(genericRulePattern, filenameRuleGroupName);
    return pattern;
  }

  @Override
  public String apply(Path file, String outputPattern) {
    var filename = file.getFileName().toString();
    return apply(filename, outputPattern);
  }

  protected String apply(String file, String outputPattern) {
    var message = String.format("apply(String, String) method not implemented for \"%s\" and apply(Path, String)"
        + " method not overriden.", this.getClass().getSimpleName());
    throw new MethodNotImplementedException(message);
  }

  @Override
  public TransformationRuleIdentity getIdentity() {
    return identity;
  }

  public String getRuleAbbreviation() {
    return ruleAbbreviation;
  }

  public String getInputRuleArguments() {
    return inputRuleArguments;
  }

  public String getOutputRuleArguments() {
    return outputRuleArguments;
  }

  public String getInputFilenamePattern() {
    return inputFilenamePattern;
  }

  public String getOutputFilenamePattern() {
    return outputFilenamePattern;
  }

  protected String getGenericRulePattern() {
    return genericRulePattern;
  }

  protected String getFilenameRuleGroupName() {
    return filenameRuleGroupName;
  }

}
