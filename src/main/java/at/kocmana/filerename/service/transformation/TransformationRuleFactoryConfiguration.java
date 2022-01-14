package at.kocmana.filerename.service.transformation;

import at.kocmana.filerename.service.transformation.rules.CreationDateTransformationRule;
import at.kocmana.filerename.service.transformation.rules.EnumerationTransformationRule;
import at.kocmana.filerename.service.transformation.rules.RegexTransformationRule;
import at.kocmana.filerename.service.transformation.rules.TimestampTransformationRule;
import java.util.List;

class TransformationRuleFactoryConfiguration {

  private TransformationRuleFactoryConfiguration() {
  }

  public static final List<TransformationRuleGenerator> FACTORY_METHODS =
      List.of(
          CreationDateTransformationRule.FACTORY_METHOD,
          EnumerationTransformationRule.FACTORY_METHOD,
          TimestampTransformationRule.FACTORY_METHOD,
          RegexTransformationRule.FACTORY_METHOD
      );

}
