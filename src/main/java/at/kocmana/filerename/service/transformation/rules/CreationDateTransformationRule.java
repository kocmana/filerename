package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.model.exception.TransformationRuleException;
import at.kocmana.filerename.service.transformation.TransformationRuleGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

public class CreationDateTransformationRule extends AbstractTransformationRule {

  private static final String RULE_SHORTCUT = "CD";
  private static final ZoneId LOCAL_TIMEZONE = TimeZone.getDefault().toZoneId();
  private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  DateTimeFormatter dtf;

  public static final TransformationRuleGenerator FACTORY_METHOD =
      CreationDateTransformationRule::generateIfRuleIsApplicable;

  private static Optional<TransformationRule> generateIfRuleIsApplicable(String inputPattern, String outputPattern) {
    if (!ruleMarkerIsPresent(RULE_SHORTCUT, outputPattern)) {
      return Optional.empty();
    }
    return Optional.of(new CreationDateTransformationRule(RULE_SHORTCUT, inputPattern, outputPattern));
  }

  private CreationDateTransformationRule(String ruleShortcut, String inputFilenamePattern,
                                         String outputFilenamePattern) {
    super(ruleShortcut, inputFilenamePattern, outputFilenamePattern);
    if (this.getOutputRuleArguments().isBlank()) {
      dtf = DEFAULT_DATE_TIME_FORMAT;
    } else {
      dtf = DateTimeFormatter.ofPattern(this.getOutputRuleArguments());
    }
  }

  public String toString() {
    var exampleCreationTimestamp = LocalDateTime.of(1990, 10, 15, 10, 35, 22, 123);
    var formattedExampleCreationTimestamp = dtf.format(exampleCreationTimestamp);
    return String.format("Creation Date Transformation Rule: Adding file creation date, will be formatted as \"%s\".",
        formattedExampleCreationTimestamp);
  }

  @Override
  public String apply(Path file, String outputPattern) {
    var creationTime = LocalDateTime.MIN;
    try {
      var fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
      var creationTimeInstant = fileAttributes.creationTime().toInstant();
      creationTime = LocalDateTime.ofInstant(creationTimeInstant, LOCAL_TIMEZONE);
    } catch (Exception exception) {
      var message = String.format("Could not determine creation time of file \"%s\": %s",
          file.getFileName().toString(), exception.getMessage());
      throw new TransformationRuleException(message, exception);
    }
    var formattedCreationTime = creationTime.format(dtf);
    return outputPattern.replaceAll(getGenericRulePattern(), formattedCreationTime);
  }
}
