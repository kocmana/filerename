package at.kocmana.filerename.service.transformation;

import at.kocmana.filerename.service.transformation.rules.TransformationRule;

import java.util.List;
import java.util.Optional;

import static at.kocmana.filerename.service.transformation.TransformationRuleFactoryConfiguration.FACTORY_METHODS;

public class TransformationRuleFactory {

  private TransformationRuleFactory(){}

  public static List<TransformationRule> generateApplicableTransformationRules(String inputPattern, String outputPattern) {

    return FACTORY_METHODS.stream()
            .map(generator -> generator.generate(inputPattern, outputPattern))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
  }

}
