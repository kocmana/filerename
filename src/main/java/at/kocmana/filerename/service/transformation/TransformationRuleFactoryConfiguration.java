package at.kocmana.filerename.service.transformation;

import at.kocmana.filerename.service.transformation.rules.EnumerationTransformationRule;
import at.kocmana.filerename.service.transformation.rules.CreationDateTransformationRule;
import at.kocmana.filerename.service.transformation.rules.TimestampTransformationRule;
import java.util.List;

class TransformationRuleFactoryConfiguration {

  private TransformationRuleFactoryConfiguration() {
  }

  public static final List<TransformationRuleGenerator> FACTORY_METHODS =
      List.of(
          TimestampTransformationRule.FACTORY_METHOD,
          EnumerationTransformationRule.FACTORY_METHOD,
          CreationDateTransformationRule.FACTORY_METHOD
      );

}
