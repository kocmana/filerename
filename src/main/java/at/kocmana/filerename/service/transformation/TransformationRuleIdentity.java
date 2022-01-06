package at.kocmana.filerename.service.transformation;

import java.util.function.BiPredicate;

public record TransformationRuleIdentity(
        int offsetFrom,
        int offsetTo
) {
  private static final BiPredicate<TransformationRuleIdentity, TransformationRuleIdentity>
          THIS_HAS_OVERLAP_STARTING_BEFORE_OTHER =
          (thisElement, otherElement) -> thisElement.offsetFrom < otherElement.offsetFrom &&
                  thisElement.offsetTo > otherElement.offsetFrom;
  private static final BiPredicate<TransformationRuleIdentity, TransformationRuleIdentity>
          OTHER_HAS_OVERLAP_STARTING_BEFORE_THIS =
          (thisElement, otherElement) -> otherElement.offsetFrom < thisElement.offsetFrom &&
                  otherElement.offsetTo > thisElement.offsetFrom;
  private static final BiPredicate<TransformationRuleIdentity, TransformationRuleIdentity>
          IDENTITIES_HAVE_PERFECT_OVERLAP =
          (thisElement, otherElement) -> thisElement.offsetFrom == otherElement.offsetFrom &&
                  thisElement.offsetTo == otherElement.offsetTo;

  public TransformationRuleIdentity {
    if (offsetFrom < 0 || offsetTo < 0) {
      throw new IllegalArgumentException("Offset values must not be < 0");
    }
    if (offsetFrom == offsetTo) {
      throw new IllegalArgumentException("Start and end offset must not be equal");
    }
  }

  boolean overlapsWith(TransformationRuleIdentity other) {

    if (other == null) {
      return false;
    }
    return THIS_HAS_OVERLAP_STARTING_BEFORE_OTHER
            .or(OTHER_HAS_OVERLAP_STARTING_BEFORE_THIS)
            .or(IDENTITIES_HAVE_PERFECT_OVERLAP)
            .test(this, other);
  }
}
