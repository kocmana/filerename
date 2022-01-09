package at.kocmana.filerename.service.transformation.rules;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnumerationTransformationRuleTest {

  @Test
  void factoryMethodReturnsInstanceIfPatternIsPresent() {
    //given + when
    var underTest = EnumerationTransformationRule.FACTORY_METHOD.generate("image.jpg", "image<<D|yyyyMMdd>><<E>>.jpg");

    //then
    assertThat(underTest)
            .isPresent()
            .containsInstanceOf(EnumerationTransformationRule.class);
  }

  @Test
  void factoryMethodReturnsInstanceIfPatternIsNotPresent() {
    //given + when
    var underTest = EnumerationTransformationRule.FACTORY_METHOD.generate("image.jpg", "image<<D|yyyyMMdd>>.jpg");

    //then
    assertThat(underTest)
            .isEmpty();
  }

  @Test
  void factoryMethodThrowsExceptionForInvalidPatterns() {
    //given + when
    var outputPattern = "image<<E|>>.jpg";
    var underTest = EnumerationTransformationRule.FACTORY_METHOD.generate("image.jpg", outputPattern);

    //then
    assertThat(underTest)
            .isEmpty();
  }

  @Test
  void getIdentity() {
  }

  @Test
  void replaceTemplateWithSearchStringDoesNotModifyInputString() {
    //given
    var inputPattern = "image.jpg";
    var outputPattern = "image<<D|yyyyMMdd>><<E>>.jpg";
    var underTestOptional = EnumerationTransformationRule.FACTORY_METHOD.generate(inputPattern, outputPattern);
    assertThat(underTestOptional).isPresent();
    var underTest = underTestOptional.orElseThrow(() -> new Error());

    //when
    var actualResult = underTest.replaceTemplateWithSearchString(inputPattern);

    //then
    assertThat(actualResult).isEqualTo(inputPattern);
  }

  @Test
  void apply() {
  }
}