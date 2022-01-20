package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.model.Region;
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

  private final String inputFilenamePattern;
  private final String outputFilenamePattern;

  private final String ruleExtractionTemplate;
  private final Pattern ruleExtractionPattern;

  private final String ruleAbbreviation;
  private final String inputRuleArguments;
  private final String outputRuleArguments;
  private final TransformationRuleIdentity identity;

  private final String filenameRuleGroupName;
  private final String genericRulePattern;

  protected AbstractTransformationRule(String ruleAbbreviation, String inputFilenamePattern,
                                       String outputFilenamePattern) {
    this.ruleAbbreviation = ruleAbbreviation;
    this.inputFilenamePattern = inputFilenamePattern;
    this.outputFilenamePattern = outputFilenamePattern;
    this.ruleExtractionTemplate = generateTemplateExtractionPattern(ruleAbbreviation);
    this.ruleExtractionPattern = Pattern.compile(ruleExtractionTemplate);
    this.genericRulePattern = generateGroupPattern(ruleAbbreviation);
    this.filenameRuleGroupName = generateFilenameGroupPattern(ruleAbbreviation);
    this.inputRuleArguments = extractGroup(inputFilenamePattern);
    this.outputRuleArguments = extractGroup(outputFilenamePattern);
    this.identity = generateIdentity();
  }

  private String generateGroupPattern(String ruleShortcut) {
    return String.format("\\<{2}%s(|.*)?\\>{2}", ruleShortcut);
  }

  private String generateFilenameGroupPattern(String ruleAbbreviation) {
    return String.format("(?<%s>.*?)", ruleAbbreviation);
  }

  private String extractGroup(String filenamePattern) {
    return ruleMatcherFor(filenamePattern)
        .map(m -> m.group("ruleArguments"))
        .orElse("");
  }

  private TransformationRuleIdentity generateIdentity() {
    var inputRange = generateIdentityRangeFromFilenamePattern(inputFilenamePattern);
    var outputRange = generateIdentityRangeFromFilenamePattern(outputFilenamePattern);
    return new TransformationRuleIdentity(inputRange, outputRange);
  }

  private Region generateIdentityRangeFromFilenamePattern(String filenamePattern) {
    return ruleMatcherFor(filenamePattern)
        .map(matcher -> new Region(matcher.start("rule"), matcher.end("rule")))
        .orElse(new Region(0, 0));
  }

  protected Optional<Matcher> ruleMatcherFor(String filenamePattern) {
    return matcherFor(ruleExtractionPattern, filenamePattern);
  }

  protected Optional<Matcher> fileNameMatcherFor(String filename) {
    var filenamePattern = Pattern.compile(replaceTemplateWithSearchString(inputFilenamePattern));
    return matcherFor(filenamePattern, filename);
  }

  protected Optional<Matcher> matcherFor(Pattern pattern, String stringToSearch) {
    if (stringToSearch == null) {
      throw new IllegalArgumentException("Can't extract matcher. Provided filename is null");
    }
    var matcher = pattern.matcher(stringToSearch);
    if (!matcher.find()) {
      return Optional.empty();
    }
    return Optional.of(matcher);
  }


  private static String generateTemplateExtractionPattern(String templateShortcut) {
    //(?<leading>.*?)(?<rule>\<{2}TS(\|(?<ruleArguments>.+))?\>{2})(?<trailing>.*?)\.(?<fileSuffix>.+)\b
    return RULE_EXTRACTION_LEADING_TEMPLATE
        + RULE_START_MARKER
        + templateShortcut
        + RULE_ARGUMENTS_MARKER
        + RULE_END_MARKER
        + RULE_EXTRACTION_TRAILING_TEMPLATE;
  }

  protected static boolean ruleMarkerIsPresent(String ruleShortcut, String filenamePattern) {
    if (filenamePattern == null || filenamePattern.isBlank()) {
      return false;
    }
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
