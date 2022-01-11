package at.kocmana.filerename.service.transformation.rules;

import static at.kocmana.filerename.testutil.TestFileUtils.openTestFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CreationDateTransformationRuleTest {

  @ParameterizedTest
  @ValueSource(strings = {"<<CD|yyyyMMdd>>", "<<CD>>", "<<CD|yyyyMMdd>><<E>>"})
  void factoryMethodReturnsInstanceIfPatternIsPresent(String ruleTemplate) {
    //given + when
    var outputPattern = String.format("image%s.jpg", ruleTemplate);
    var underTest =
        CreationDateTransformationRule.FACTORY_METHOD.generate("image.jpg", outputPattern);

    //then
    assertThat(underTest)
        .isPresent()
        .containsInstanceOf(CreationDateTransformationRule.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"<<C>>", "<<CD|>>", "<<E>>"})
  @EmptySource
  void factoryMethodReturnsInstanceIfPatternIsNotPresent(String ruleTemplate) {
    //given + when
    var outputPattern = String.format("image%s.jpg", ruleTemplate);
    var underTest = CreationDateTransformationRule.FACTORY_METHOD.generate("image<<CD>>.jpg", outputPattern);

    //then
    assertThat(underTest)
        .isEmpty();
  }

  @Test
  void factoryMethodThrowsExceptionForInvalidPatterns() {
    //given
    var outputPattern = "image<<CD|cmykTTTT>>.jpg";

    //when + then
    assertThatIllegalArgumentException().isThrownBy(
            () -> CreationDateTransformationRule.FACTORY_METHOD.generate("image.jpg", outputPattern))
        .withMessage("Unknown pattern letter: T");
  }

  @Test
  void applyWithoutArgumentsGeneratesCorrectFilename() {
    //given
    Path file = Paths.get("src", "test", "resources", "testfiles", "testfile.img");

    var outputPattern = "image<<CD>>.jpg";
    var underTestOptional = CreationDateTransformationRule.FACTORY_METHOD.generate("image.jpg", outputPattern);

    //when
    assertThat(underTestOptional).isPresent();
    var underTest = underTestOptional.orElseThrow(Error::new);
    var actualResult = underTest.apply(file, outputPattern);

    //then
    assertThat(actualResult).isEqualTo("image2022-01-11T15:17:59.1355439.jpg");
  }

  @Test
  void applyWithArgumentGeneratesCorrectFilename() {
    //given
    Path file = openTestFile("testfile.img");

    var outputPattern = "image<<CD|dd-MM-yyyy>>.jpg";
    var underTestOptional = CreationDateTransformationRule.FACTORY_METHOD.generate("image.jpg", outputPattern);

    //when
    assertThat(underTestOptional).isPresent();
    var underTest = underTestOptional.orElseThrow(Error::new);
    var actualResult = underTest.apply(file, outputPattern);

    //then
    assertThat(actualResult).isEqualTo("image11-01-2022.jpg");
  }
}
