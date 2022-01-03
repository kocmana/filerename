package at.kocmana.filerename.service.transformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.*;

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
    var actualResult = TimestampTransformationRule.generateIfRuleIsApplicable(filename, filename);

    assertThat(actualResult).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
      "foo<<dd>>.jpg, bar<<dd>>.png"
  })
  void generateIfRuleIsApplicableReturnsInstance(String inputPattern, String outputPattern) {
    var actualResult = TimestampTransformationRule.generateIfRuleIsApplicable(inputPattern, outputPattern);

    assertThat(actualResult).isPresent();
    System.out.println(actualResult);
  }

  @Test
  void exceptionIsThrownIfOutputTargetDoesNotContainDateTemplate(){
    assertThatIllegalArgumentException().isThrownBy(()->TimestampTransformationRule.generateIfRuleIsApplicable("foo<<dd>>.jpg", "bar.jpg"))
        .withMessage("Date template found in source pattern but not target pattern.");
  }
}
