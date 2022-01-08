package at.kocmana.filerename.service.transformation;

import at.kocmana.filerename.service.transformation.rules.TransformationRule;

import java.util.Optional;

@FunctionalInterface
public interface TransformationRuleGenerator {

  Optional<TransformationRule> generate(String inputPattern, String outputPattern);

}
