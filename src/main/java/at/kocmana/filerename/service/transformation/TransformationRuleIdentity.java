package at.kocmana.filerename.service.transformation;

import static java.util.Objects.isNull;

import java.util.function.BiPredicate;

public record TransformationRuleIdentity(
    int offsetFrom,
    int offsetTo
) {
  private static final BiPredicate<TransformationRuleIdentity, TransformationRuleIdentity>
      thisIdentityHasOverlapStartingBeforeOther =
      (thisElement, otherElement) -> thisElement.offsetFrom < otherElement.offsetFrom &&
          thisElement.offsetTo > otherElement.offsetFrom;
  private static final BiPredicate<TransformationRuleIdentity, TransformationRuleIdentity>
      otherIdentityHasOverlapStartingBeforeThis =
      (thisElement, otherElement) -> otherElement.offsetFrom < thisElement.offsetFrom &&
          otherElement.offsetTo > thisElement.offsetFrom;
  private static final BiPredicate<TransformationRuleIdentity, TransformationRuleIdentity>
      identitiesHavePerfectOverlap =
      (thisElement, otherElement) -> thisElement.offsetFrom == otherElement.offsetFrom &&
          thisElement.offsetTo == otherElement.offsetTo;

  public TransformationRuleIdentity(int offsetFrom, int offsetTo) {
    if (offsetFrom < 0 || offsetTo < 0) {
      throw new IllegalArgumentException("Offset values must not be < 0");
    }
    if (offsetFrom == offsetTo) {
      throw new IllegalArgumentException("Start and end offset must not be equal");
    }

    this.offsetFrom = offsetFrom;
    this.offsetTo = offsetTo;
  }

  boolean overlapsWith(TransformationRuleIdentity other) {

    if (isNull(other)) {
      return false;
    }
    return thisIdentityHasOverlapStartingBeforeOther
        .or(otherIdentityHasOverlapStartingBeforeThis)
        .or(identitiesHavePerfectOverlap)
        .test(this, other);
  }
}
