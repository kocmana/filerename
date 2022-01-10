package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.service.FileRenameTask;
import at.kocmana.filerename.service.transformation.TransformationRuleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnumerationTransformationRule implements TransformationRule {

  private static final String ENUMERATION_FORMAT_EXTRACTION_TEMPLATE = "(?<leading>.*?)(<{2}E(\\|(?<enumerationFormat>.+))?>{2})(?<trailing>.*?).(?<fileSuffix>.+)\\b";
  private static final Pattern ENUMERATION_FORMAT_EXTRACTION_PATTERN = Pattern.compile(ENUMERATION_FORMAT_EXTRACTION_TEMPLATE);
  private static final String ENUMERATION_TRANSFORMATION_RULE_MARKER = "<{2}E\\|*.?>{2}";

  private static final String DEFAULT_ENUMERATION_FORMAT = "%d";

  private final String enumerationFormat;
  private final AtomicInteger currentIndex = new AtomicInteger(0);

  TransformationRuleIdentity identity;

  @Override
  public TransformationRuleIdentity getIdentity() {
    return this.identity;
  }

  private static final Logger log = LoggerFactory.getLogger(FileRenameTask.class);

  public static final TransformationRuleGenerator FACTORY_METHOD = EnumerationTransformationRule::generateIfRuleIsApplicable;

  public EnumerationTransformationRule(String enumerationFormat, TransformationRuleIdentity identity) {
    this.enumerationFormat = enumerationFormat;
    this.identity = identity;
  }

  public String toString() {
    var exampleNumber = 12;
    var formattedExampleNumber = String.format(enumerationFormat, exampleNumber);
    return String.format("Enumeration Transformation Rule: Adding enumeration with pattern \"%s\": \"%d\" will be displayed as \"%s\".",
            enumerationFormat, exampleNumber, formattedExampleNumber);
  }

  private static Optional<TransformationRule> generateIfRuleIsApplicable(String inputPattern,
                                                                         String outputPattern) {
    if (inputPattern == null || outputPattern == null) {
      return Optional.empty();
    }

    var outputEnumerationMatcher = ENUMERATION_FORMAT_EXTRACTION_PATTERN.matcher(outputPattern);
    if (!outputEnumerationMatcher.find()) {
      return Optional.empty();
    }

    var enumerationFormat = extractNumberFormat(outputEnumerationMatcher);
    var identity = new TransformationRuleIdentity(outputEnumerationMatcher.start(), outputEnumerationMatcher.end());

    return Optional.of(new EnumerationTransformationRule(enumerationFormat, identity));
  }

  private static String extractNumberFormat(Matcher enumerationMatcher) {
    var numberFormat = enumerationMatcher.group("enumerationFormat");
    if(numberFormat == null || numberFormat.isBlank()) {
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

  private static String toSearchString(String argumentPattern) {
    //since this transformation rule only affects the output pattern, just return the original argument pattern.
    return argumentPattern;
  }

  @Override
  public String apply(Path file, String outputPattern) {
    var filename = file.getFileName().toString();
    log.warn("Filename: {}, outputPattern: {}", filename, outputPattern);

    var formattedIndex = String.format(enumerationFormat, this.currentIndex.getAndIncrement());
    return outputPattern.replaceAll(ENUMERATION_TRANSFORMATION_RULE_MARKER, formattedIndex);
  }
}
