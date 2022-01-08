package at.kocmana.filerename.service.transformation;

import at.kocmana.filerename.service.transformation.rules.TimestampTransformationRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TransformationRuleFactoryTest {

  @Test
  void assertThatRuleFactoryCreatesTimestampTransformationRules() {
    //given
    var inputPattern = "foo<<yyyyMMdd>>.bar";
    var ouputPattern = "foo<<ddMMyyyy>>.bar";

    //when
    var actualResult = TransformationRuleFactory.generateApplicableTransformationRules(inputPattern, ouputPattern);

    //then
    assertThat(actualResult).hasSize(1);
    var firstRule = actualResult.get(0);
    assertThat(firstRule).isInstanceOf(TimestampTransformationRule.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"foo.bar", "foo<bar>.foo"})
  @NullAndEmptySource
  void assertThatRuleFactoryCreatesNoRulesIfNoneAreApplicable(String pattern) {
    //when
    var actualResult = TransformationRuleFactory.generateApplicableTransformationRules(pattern, pattern);

    //then
    assertThat(actualResult).isEmpty();
  }

}