package at.kocmana.filerename.service.transformation.rules;

import static at.kocmana.filerename.service.transformation.rules.RegexTransformationRule.FACTORY_METHOD;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RegexTransformationRuleTest {

  @ParameterizedTest
  @ValueSource(strings = {"<>.bar", "foo.bar"})
  @NullAndEmptySource
  void generateIfRuleIsApplicableReturnsEmptyForPatternsWithMissingTemplate(String filename) {
    var actualResult = FACTORY_METHOD.generate(filename, filename);

    assertThat(actualResult).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
      "foo<<R|[0-9]{4}>>.jpg, bar<<R>>.png"
  })
  void generateIfRuleIsApplicableReturnsInstance(String inputPattern, String outputPattern) {
    var actualResult = FACTORY_METHOD.generate(inputPattern, outputPattern);

    assertThat(actualResult).isPresent();
  }

  @Test
  void ruleInstanceIsReturnedIfOutputTargetDoesNotContainDateTemplate() {
    var actualResult = FACTORY_METHOD.generate("foo<<R|[a-z]+>>.jpg", "bar.jpg");

    assertThat(actualResult).isPresent();
  }

  @Test
  void ruleInstanceIsReturnedIfInputTargetDoesNotContainDateTemplate() {
    var actualResult = FACTORY_METHOD.generate("bar.jpg", "foo<<R|[a-z]+>>.jpg");

    assertThat(actualResult).isEmpty();
  }


  @Test
  void transformToSearchPatternWorksAsExpected() {
    var underTest = FACTORY_METHOD.generate("foo<<R|[a-zA-Z]{3}>>.bar", "<<R>>bar.foo");
    assertThat(underTest).isPresent();
    var transformationRule = underTest.orElseThrow(UnknownError::new);

    var actualResult = transformationRule.replaceTemplateWithSearchString("foo<<R|[a-zA-Z]{3}>>.bar");

    assertThat(actualResult).isEqualTo("foo(?<R>[a-zA-Z]{3}).bar");
  }

}
