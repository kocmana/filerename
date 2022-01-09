package at.kocmana.filerename.service.transformation.rules;

import java.util.regex.Pattern;

public abstract class AbstractTransformationRule implements TransformationRule {

  private static final String RULE_EXTRACTION_LEADING_TEMPLATE = "(?<leading>.*?)(";
  private static final String RULE_EXTRACTION_TRAILING_TEMPLATE = ")(?<trailing>.*?)\\.(?<fileSuffix>.+)\\b";
  private static final String RULE_START_MARKER = "<{2}";
  private static final String RULE_END_MARKER = ">{2}";
  private static final String RULE_ARGUMENTS_MARKER = "\\(|(?<ruleArguments>.+))?";

  private final String ruleMarker;
  private final String inputRuleArguments;
  private final String outputRuleArguments;
  private String ruleExtractionTemplate;
  private Pattern ruleExtractionPattern;

  private String inputFilenamePattern;
  private String outputFilenamePattern;

  protected AbstractTransformationRule(String templateShortcut, String inputFilenamePattern, String outputFilenamePattern) {
    this.ruleMarker = templateShortcut;
    ruleExtractionTemplate = generateTemplateExtractionPattern(templateShortcut);
    ruleExtractionPattern = Pattern.compile(ruleExtractionTemplate);
    inputRuleArguments = extractRuleArguments(inputFilenamePattern);
    outputRuleArguments = extractRuleArguments(outputFilenamePattern);
  }

  private String extractRuleArguments(String filenamePattern) {
    var matcher = ruleExtractionPattern.matcher(inputFilenamePattern);
    if (!matcher.find()) {
      var message = String.format("Filename pattern \"%s\" does not contain rule marker \"%s\".", filenamePattern, ruleMarker);
      throw new IllegalArgumentException(message);
    }
    return matcher.group("ruleArguments");
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

  protected static boolean ruleMarkerIsPresent(String ruleMarker, String filenamePattern) {
    var ruleExtractionPattern = Pattern.compile(generateTemplateExtractionPattern(ruleMarker));
    var matcher = ruleExtractionPattern.matcher(filenamePattern);
    return matcher.find();
  }

  public String getRuleMarker() {
    return ruleMarker;
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

}
