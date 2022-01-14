package at.kocmana.filerename.service.transformation.rules;

import java.util.function.BiPredicate;

public record TransformationRuleIdentity(
    Range inputRange,
    Range outputRange
) {

  public TransformationRuleIdentity {
    if (inputRange.length() == 0 && outputRange.length() == 0) {
      throw new IllegalArgumentException(
          "TransformationRuleIdentity has zero-length ranges both for input and output pattern.");
    }
  }

  public TransformationRuleIdentity(int inputOffsetFrom, int inputOffsetTo, int outputOffsetFrom, int outputOffsetTo) {
    this(new Range(inputOffsetFrom, inputOffsetTo),
        new Range(outputOffsetFrom, outputOffsetTo));
  }

  boolean overlapsWith(TransformationRuleIdentity other) {
    if (other == null) {
      return false;
    }
    return this.inputRange.overlapsWith(other.inputRange) ||
        this.outputRange.overlapsWith(other.outputRange);
  }

  public record Range(
      int offsetFrom,
      int offsetTo
  ) {
    private static final BiPredicate<Range, Range>
        THIS_HAS_OVERLAP_STARTING_BEFORE_OTHER =
        (thisElement, otherElement) -> thisElement.offsetFrom < otherElement.offsetFrom &&
            thisElement.offsetTo > otherElement.offsetFrom;
    private static final BiPredicate<Range, Range>
        OTHER_HAS_OVERLAP_STARTING_BEFORE_THIS =
        (thisElement, otherElement) -> otherElement.offsetFrom < thisElement.offsetFrom &&
            otherElement.offsetTo > thisElement.offsetFrom;
    private static final BiPredicate<Range, Range>
        IDENTITIES_HAVE_PERFECT_OVERLAP =
        (thisElement, otherElement) -> thisElement.offsetFrom == otherElement.offsetFrom &&
            thisElement.offsetTo == otherElement.offsetTo;

    public Range {
      if (offsetFrom < 0 || offsetTo < 0) {
        throw new IllegalArgumentException("Offset values must not be < 0");
      }
      if (offsetTo < offsetFrom) {
        throw new IllegalArgumentException(String.format("Starting offset (%d) was smaller than ending offset (%d)",
            offsetFrom, offsetTo));
      }
    }

    private int length() {
      return offsetTo - offsetFrom;
    }

    private boolean overlapsWith(Range other) {
      if (other == null) {
        return false;
      }

      //Zero length identities can't have overlaps
      if (this.length() == 0 || other.length() == 0) {
        return false;
      }

      return THIS_HAS_OVERLAP_STARTING_BEFORE_OTHER
          .or(Range.OTHER_HAS_OVERLAP_STARTING_BEFORE_THIS)
          .or(Range.IDENTITIES_HAVE_PERFECT_OVERLAP)
          .test(this, other);
    }
  }
}
