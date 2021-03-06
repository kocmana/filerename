package at.kocmana.filerename.service.transformation.rules;

import static at.kocmana.filerename.service.transformation.rules.TimestampTransformationRule.FACTORY_METHOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TimestampTransformationRuleTest {

  @ParameterizedTest
  @ValueSource(strings = {"<>.bar","foo.bar"})
  @NullAndEmptySource
  void generateIfRuleIsApplicableReturnsEmptyForPatternsWithMissingTemplate(String filename) {
    var actualResult = FACTORY_METHOD.generate(filename, filename);

    assertThat(actualResult).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
      "foo<<TS|dd>>.jpg, bar<<TS|dd>>.png"
  })
  void generateIfRuleIsApplicableReturnsInstance(String inputPattern, String outputPattern) {
    var actualResult = FACTORY_METHOD.generate(inputPattern, outputPattern);

    assertThat(actualResult).isPresent();
  }

  @Test
  void exceptionIsThrownIfOutputTargetDoesNotContainDateTemplate(){
    var actualResult = FACTORY_METHOD.generate("foo<<TS|dd>>.jpg", "bar.jpg");

    assertThat(actualResult).isEmpty();
  }

  @Test
  void transformToSearchPatternWorksAsExpected() {
    var underTest = FACTORY_METHOD.generate("foo<<TS|yyyyMMdd>>.bar", "<<TS|ddMMyyyy>>bar.foo");
    assertThat(underTest).isPresent();
    var transformationRule = underTest.orElseThrow(UnknownError::new);

    var actualResult = transformationRule.replaceTemplateWithSearchString("foo<<TS|yyyyMMdd>>.bar");

    assertThat(actualResult).isEqualTo("foo(?<TS>.*?).bar");
  }
}
