package uk.co.fractalmotion.mugshot.preview.runtime

/**
 * The rendering configuration one generated snapshot is taken at.
 *
 * Deliberately plain data rather than a `DeviceConfig`. This module sits on the *main* compile
 * classpath, where the `mugshot` artifact is not available — it is a test-only dependency — so the
 * generated preview catalogue cannot name Mugshot's own types. `mugshot-preview-junit` turns this
 * into a rule on the test side.
 */
public data class MugshotPreviewConfig(
  val device: MugshotPreviewDevice = MugshotPreviewDevice.DEFAULT,
  val nightMode: Boolean = false,
  val fontScale: Float = 1f,
  val locale: String? = null,
  val rtl: Boolean = false,
  val renderingMode: MugshotPreviewRenderingMode = MugshotPreviewRenderingMode.NORMAL
)

/** Device shapes, mirroring `uk.co.fractalmotion.mugshot.annotations.MugshotDevice`. */
public enum class MugshotPreviewDevice {
  DEFAULT,
  PHONE,
  FOLDABLE,
  TABLET,
  LANDSCAPE,
  WEAR_ROUND,
  WEAR_SQUARE
}

/** Mirrors layoutlib's `SessionParams.RenderingMode`, which this module cannot reference. */
public enum class MugshotPreviewRenderingMode {
  NORMAL,
  SHRINK,
  V_SCROLL
}
