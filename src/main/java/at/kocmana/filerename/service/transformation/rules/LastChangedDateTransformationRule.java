package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.service.transformation.TransformationRuleGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

public class LastChangedDateTransformationRule extends AbstractTransformationRule {

  private static final String RULE_SHORTCUT = "CD";
  private static final ZoneId LOCAL_TIMEZONE = TimeZone.getDefault().toZoneId();

  DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;

  public static final TransformationRuleGenerator FACTORY_METHOD = LastChangedDateTransformationRule::generateIfRuleIsApplicable;

  private static Optional<TransformationRule> generateIfRuleIsApplicable(String inputPattern, String outputPattern) {
    if (!ruleMarkerIsPresent(RULE_SHORTCUT, outputPattern)) {
      return Optional.empty();
    }
    return Optional.of(new LastChangedDateTransformationRule(RULE_SHORTCUT, inputPattern, outputPattern));
  }

  private LastChangedDateTransformationRule(String ruleShortcut, String inputFilenamePattern, String outputFilenamePattern) {
    super(ruleShortcut, inputFilenamePattern, outputFilenamePattern);
    if (this.getOutputRuleArguments().isBlank()) {
      dtf = DateTimeFormatter.ISO_DATE_TIME;
    } else {
      dtf = DateTimeFormatter.ofPattern(this.getOutputRuleArguments());
    }
  }

  @Override
  public String apply(Path file, String outputPattern) {
    var creationTime = LocalDateTime.MIN;
    try {
      var fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
      var creationTimeInstant = fileAttributes.creationTime().toInstant();
      creationTime = LocalDateTime.ofInstant(creationTimeInstant, LOCAL_TIMEZONE);
    } catch (Exception exception) {
      throw new RuntimeException(exception); //TODO add own exception type here
    }
    var formattedCreationTime = creationTime.format(dtf);
    return outputPattern.replaceAll(getFilenameRuleGroupName(), formattedCreationTime);
  }
}
