package at.kocmana.filerename.service.transformation;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

class TransformationRuleFactoryConfiguration {

  private TransformationRuleFactoryConfiguration() {
  }

  public static final List<BiFunction<String, String, Optional<TransformationRule>>> FACTORY_METHODS =
          List.of(
                  TimestampTransformationRule.FACTORY_METHOD
          );

}
