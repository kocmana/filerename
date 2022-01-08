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
      "foo<<dd>>.jpg, bar<<dd>>.png"
  })
  void generateIfRuleIsApplicableReturnsInstance(String inputPattern, String outputPattern) {
    var actualResult = FACTORY_METHOD.generate(inputPattern, outputPattern);

    assertThat(actualResult).isPresent();
  }

  @Test
  void exceptionIsThrownIfOutputTargetDoesNotContainDateTemplate(){
    assertThatIllegalArgumentException().isThrownBy(()-> FACTORY_METHOD.generate("foo<<dd>>.jpg", "bar.jpg"))
        .withMessage("Date template found in source pattern but not target pattern.");
  }

  @Test
  void transformToSearchPatternWorksAsExpected() {
    var underTest = FACTORY_METHOD.generate("foo<<yyyyMMdd>>.bar", "<<ddMMyyyy>>bar.foo");
    assertThat(underTest).isPresent();
    var transformationRule = underTest.orElseThrow(UnknownError::new);

    var actualResult = transformationRule.replaceTemplateWithSearchString("foo<<yyyyMMdd>>.bar");

    assertThat(actualResult).isEqualTo("foo(?<date>.*?).bar");
  }
}
