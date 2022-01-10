package at.kocmana.filerename.service.transformation.rules;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTransformationRule implements TransformationRule {

  private static final String RULE_EXTRACTION_LEADING_TEMPLATE = "(?<leading>.*?)(";
  private static final String RULE_EXTRACTION_TRAILING_TEMPLATE = ")(?<trailing>.*?)\\.(?<fileSuffix>.+)\\b";
  private static final String RULE_START_MARKER = "(?<rule>\\<{2}";
  private static final String RULE_ARGUMENTS_MARKER = "\\(|(?<ruleArguments>.+))?";
  private static final String RULE_END_MARKER = "\\>{2})";

  private final String ruleExtractionTemplate;
  private final Pattern ruleExtractionPattern;

  private final String ruleShortcut;
  private final String genericRulePattern;
  private final String inputRuleArguments;
  private final String outputRuleArguments;
  private final String filenameRuleGroupName;

  private String inputFilenamePattern;
  private String outputFilenamePattern;

  private final TransformationRuleIdentity identity;

  protected AbstractTransformationRule(String ruleShortcut, String inputFilenamePattern, String outputFilenamePattern) {
    this.ruleShortcut = ruleShortcut;
    ruleExtractionTemplate = generateTemplateExtractionPattern(ruleShortcut);
    ruleExtractionPattern = Pattern.compile(ruleExtractionTemplate);
    genericRulePattern = generateGroupPattern(ruleShortcut);
    filenameRuleGroupName = generateFilenameGroupPattern(ruleShortcut);
    inputRuleArguments = extractGroup(inputFilenamePattern);
    outputRuleArguments = extractGroup(outputFilenamePattern);
    identity = generateIdentity();
  }

  private String generateGroupPattern(String ruleShortcut) {
    return String.format("\\<{2}%s(|.*)?\\>{2}", ruleShortcut);
  }

  private String generateFilenameGroupPattern(String ruleShortcut) {
    return String.format("?<%s.*>", ruleShortcut);
  }

  private String extractGroup(String filenamePattern) {
    Matcher matcher = matcherFor(filenamePattern);
    return matcher.group("ruleArguments");
  }

  private TransformationRuleIdentity generateIdentity() {
    var matcher = matcherFor(outputFilenamePattern);
    return new TransformationRuleIdentity(matcher.start("rule"), matcher.end("rule"));
  }

  private Matcher matcherFor(String filenamePattern) {
    var matcher = ruleExtractionPattern.matcher(filenamePattern);
    if (!matcher.find()) {
      var message = String.format("Filename pattern \"%s\" does not contain rule marker \"%s\".", filenamePattern, ruleShortcut);
      throw new IllegalArgumentException(message);
    }
    return matcher;
  }

  private static String generateTemplateExtractionPattern(String templateShortcut) {
    //(?<leading>.*?)(<{2}TS(\|(?<dateFormat>.+))?>{2})(?<trailing>.*?)\.(?<fileSuffix>.+)\b
    return RULE_EXTRACTION_LEADING_TEMPLATE
            + RULE_START_MARKER
            + templateShortcut
            + RULE_ARGUMENTS_MARKER
            + RULE_END_MARKER
            + RULE_EXTRACTION_TRAILING_TEMPLATE;
  }

  protected static boolean ruleMarkerIsPresent(String ruleShortcut, String filenamePattern) {
    var ruleExtractionPattern = Pattern.compile(generateTemplateExtractionPattern(ruleShortcut));
    var matcher = ruleExtractionPattern.matcher(filenamePattern);
    return matcher.find();
  }

  @Override
  public String replaceTemplateWithSearchString(String pattern) {
    return pattern.replaceAll(genericRulePattern, filenameRuleGroupName);
  }

  @Override
  public String apply(Path file, String outputPattern) {
    var filename = file.getFileName().toString();
    return apply(filename, outputPattern);
  }

  protected String apply(String file, String outputPattern) {
    var message = String.format("apply(String, String) method not implemented for \"%s\" and apply(Path, String)"
            + " method not overriden.", this.getClass().getSimpleName());
    throw new ExceptionInInitializerError(message); //TODO create own MethodNotImplemented Exception
  }

  @Override
  public TransformationRuleIdentity getIdentity() {
    return identity;
  }

  public String getRuleShortcut() {
    return ruleShortcut;
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
