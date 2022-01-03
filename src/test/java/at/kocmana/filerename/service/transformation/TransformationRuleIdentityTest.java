package at.kocmana.filerename.service.transformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TransformationRuleIdentityTest {

  @Test
  void testConstructorDoesNotAcceptZeroDistance() {
    assertThatIllegalArgumentException().isThrownBy(() -> new TransformationRuleIdentity(1, 1))
        .withMessage("Start and end offset must not be equal");
  }

  @ParameterizedTest
  @CsvSource({
      "0,-1",
      "-1,0",
      "-1,-1"
  })
  void testConstructorDoesNotAcceptNegativeValues(int offsetFrom, int offsetTo) {
    assertThatIllegalArgumentException().isThrownBy(() -> new TransformationRuleIdentity(offsetFrom, offsetTo))
        .withMessage("Offset values must not be < 0");

  }

  @ParameterizedTest
  @CsvSource(value = {
      "1,3,2,4", // this starts before other, overlapping
      "2,4,1,3", // other starts before this, overlapping
      "1,4,2,3", // this embodies other
      "2,3,1,4", // other embodies this
      "1,3,1,3," // total overlap
  })
  void overlapsWithReturnsTrueIfOverlapExists(int thisOffsetFrom, int thisOffsetTo, int otherOffsetFrom,
                                              int otherOffsetTo) {
    //given
    var underTest = new TransformationRuleIdentity(thisOffsetFrom, thisOffsetTo);
    var other = new TransformationRuleIdentity(otherOffsetFrom, otherOffsetTo);

    //when
    var actualResult = underTest.overlapsWith(other);

    //then
    assertThat(actualResult).isTrue();
  }


  @ParameterizedTest
  @CsvSource(value = {
      "1,2,2,3", // this starts directly before other
      "2,3,1,2", // other starts directly before this
      "1,2,4,5", // this starts with distance before other
      "4,5,1,2" // other starts with distance before this
  })
  void overlapsWithReturnsFalseIfNoOverlapExists(int thisOffsetFrom, int thisOffsetTo, int otherOffsetFrom,
                                                 int otherOffsetTo) {
    //given
    var underTest = new TransformationRuleIdentity(thisOffsetFrom, thisOffsetTo);
    var other = new TransformationRuleIdentity(otherOffsetFrom, otherOffsetTo);

    //when
    var actualResult = underTest.overlapsWith(other);

    //then
    assertThat(actualResult).isFalse();
  }
}
