package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.model.Region;

public record TransformationRuleIdentity(
    Region inputRange,
    Region outputRange
) {

  public TransformationRuleIdentity {
    if (inputRange.length() == 0 && outputRange.length() == 0) {
      throw new IllegalArgumentException(
          "TransformationRuleIdentity has zero-length ranges both for input and output pattern.");
    }
  }

  public TransformationRuleIdentity(int inputOffsetFrom, int inputOffsetTo, int outputOffsetFrom, int outputOffsetTo) {
    this(new Region(inputOffsetFrom, inputOffsetTo),
        new Region(outputOffsetFrom, outputOffsetTo));
  }

  boolean overlapsWith(TransformationRuleIdentity other) {
    if (other == null) {
      return false;
    }
    return this.inputRange.overlapsWith(other.inputRange) ||
        this.outputRange.overlapsWith(other.outputRange);
  }
}
