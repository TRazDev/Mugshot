package uk.co.fractalmotion.mugshot.sample.catalog

import com.android.resources.ScreenOrientation
import uk.co.fractalmotion.mugshot.DeviceConfig

/**
 * The devices the sample renders on.
 *
 * [PHONE] is reserved for the goldens where visual quality is the point — the generated previews,
 * the plain screen recipe and dark mode. Everything else uses [COMPACT], which has roughly a
 * quarter of the pixels, because a variant matrix is checking layout behaviour rather than showing
 * the design off, and lossless WebP goldens are committed to the repository.
 */
enum class SampleDevice(val config: DeviceConfig) {
  PHONE(DeviceConfig.PIXEL_6),
  COMPACT(DeviceConfig.NEXUS_4),
  TABLET(DeviceConfig.PIXEL_TABLET),
  FOLD(DeviceConfig.PIXEL_FOLD),
  LANDSCAPE(DeviceConfig.PIXEL_6.copy(orientation = ScreenOrientation.LANDSCAPE))
}
