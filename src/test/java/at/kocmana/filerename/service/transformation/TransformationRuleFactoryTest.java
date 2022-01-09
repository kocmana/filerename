package at.kocmana.filerename.service.transformation;

import at.kocmana.filerename.service.transformation.rules.EnumerationTransformationRule;
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
    var inputPattern = "foo<<TS|yyyyMMdd>>.bar";
    var ouputPattern = "foo<<TS|ddMMyyyy>>.bar";

    //when
    var actualResult = TransformationRuleFactory.generateApplicableTransformationRules(inputPattern, ouputPattern);

    //then
    assertThat(actualResult).hasSize(1);
    var firstRule = actualResult.get(0);
    assertThat(firstRule).isInstanceOf(TimestampTransformationRule.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"<<E>>", "<<E|%d>>"})
  void assertThatRuleFactoryCreatesEnumerationTransformationRules(String template) {
    //given
    var inputPattern = "foo.bar";
    var ouputPattern = String.format("foo%s.bar", template);

    //when
    var actualResult = TransformationRuleFactory.generateApplicableTransformationRules(inputPattern, ouputPattern);

    //then
    assertThat(actualResult).hasSize(1);
    var firstRule = actualResult.get(0);
    assertThat(firstRule).isInstanceOf(EnumerationTransformationRule.class);
  }

  @Test
  void assertThatRuleFactoryCreatesMultipleEnumerationTransformationRules() {
    //given
    var inputPattern = "foo<<TS|yyyyMMdd>><<E>>.bar";
    var ouputPattern = "bar<<TS|dd-MM-yyyy>><<E>>.foo";

    //when
    var actualResult = TransformationRuleFactory.generateApplicableTransformationRules(inputPattern, ouputPattern);

    //then
    assertThat(actualResult).hasSize(2);
    var firstRule = actualResult.get(0);
    assertThat(firstRule).isInstanceOf(TimestampTransformationRule.class);
    var secondRule = actualResult.get(1);
    assertThat(secondRule).isInstanceOf(EnumerationTransformationRule.class);

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