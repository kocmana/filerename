package at.kocmana.filerename.service.transformation.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FileEnumeraterTest {

  @Test
  void forFilenameCallsCorrectConstructor() {
    //given
    var expectedFilename = "foo.bar";

    //when
    var underTest = FileEnumerater.forFilename("foo.bar");
    var actualFilename = underTest.getFilename();

    //then
    assertThat(actualFilename).isEqualTo(expectedFilename);
  }

  @ParameterizedTest
  @ValueSource(strings = {" ", "    "})
  @NullAndEmptySource
  void forFilenameThrowsExceptionForNullOrBlankFilenames(String illegalFilename) {

    assertThatIllegalArgumentException().isThrownBy(() -> FileEnumerater.forFilename(illegalFilename))
        .withMessage("Filename is null or blank")
        .withNoCause();
  }

  @Test
  void enumerateFilename() {
    //given
    var filename = "foo.bar";
    var underTest = FileEnumerater.forFilename(filename);

    for (int i = 1; i < 100; i++) {
      var expectedFilename = String.format("foo-%d.bar", i);

      //when
      var actualFilename = underTest.enumerateFilename();

      //then
      assertThat(actualFilename).isEqualTo(expectedFilename);
    }
  }
}
