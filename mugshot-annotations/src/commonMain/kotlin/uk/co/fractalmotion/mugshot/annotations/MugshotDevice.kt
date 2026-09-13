package uk.co.fractalmotion.mugshot.annotations

/**
 * The device shapes the [MugshotDevices] axis can render on.
 *
 * Deliberately a short list of *shapes* rather than a catalogue of handsets: the point of the axis
 * is to catch layouts that break when the viewport changes proportion, and a second 6.1" phone
 * proves nothing that the first one did not.
 */
public enum class MugshotDevice {
  PHONE,
  FOLDABLE,
  TABLET,
  LANDSCAPE,
  WEAR_ROUND,
  WEAR_SQUARE
}
