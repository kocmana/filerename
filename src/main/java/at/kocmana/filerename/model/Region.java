package at.kocmana.filerename.model;

import java.util.function.BiPredicate;

public record Region(int offsetFrom, int offsetTo) {
  private static final BiPredicate<Region, Region>
      THIS_HAS_OVERLAP_STARTING_BEFORE_OTHER =
      (thisElement, otherElement) -> thisElement.offsetFrom < otherElement.offsetFrom &&
          thisElement.offsetTo > otherElement.offsetFrom;
  private static final BiPredicate<Region, Region>
      OTHER_HAS_OVERLAP_STARTING_BEFORE_THIS =
      (thisElement, otherElement) -> otherElement.offsetFrom < thisElement.offsetFrom &&
          otherElement.offsetTo > thisElement.offsetFrom;
  private static final BiPredicate<Region, Region>
      IDENTITIES_HAVE_PERFECT_OVERLAP =
      (thisElement, otherElement) -> thisElement.offsetFrom == otherElement.offsetFrom &&
          thisElement.offsetTo == otherElement.offsetTo;

  public Region {
    if (offsetFrom < 0 || offsetTo < 0) {
      throw new IllegalArgumentException("Offset values must not be < 0");
    }
    if (offsetTo < offsetFrom) {
      throw new IllegalArgumentException(String.format("Starting offset (%d) was smaller than ending offset (%d)",
          offsetFrom, offsetTo));
    }
  }

  public int length() {
    return offsetTo - offsetFrom;
  }

  public boolean overlapsWith(Region other) {
    if (other == null) {
      return false;
    }

    //Zero length identities can't have overlaps
    if (this.length() == 0 || other.length() == 0) {
      return false;
    }

    return THIS_HAS_OVERLAP_STARTING_BEFORE_OTHER
        .or(Region.OTHER_HAS_OVERLAP_STARTING_BEFORE_THIS)
        .or(Region.IDENTITIES_HAVE_PERFECT_OVERLAP)
        .test(this, other);
  }
}

