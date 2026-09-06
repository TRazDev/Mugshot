package uk.co.fractalmotion.mugshot.internal.differs

/**
 * The colours a differ paints into the delta image.
 *
 * The delta image marks where two renders disagree, so each pixel gets one of these rather than a
 * shade mixed from the two inputs. A reader should be able to tell what a pixel means from its
 * colour alone.
 */
internal object DeltaColors {
  /**
   * Nothing to show here: the renders agree, both left the pixel transparent, or they differ by
   * little enough that the differ lets it pass.
   */
  const val UNCHANGED: Int = 0x00808080

  /**
   * The two renders disagree here by more than the differ allows.
   *
   * Ruby rather than pure red, which reads as a warning colour and vibrates against a white
   * background.
   */
  const val DIFFERENT: Int = 0xFF9B111E.toInt()
}
