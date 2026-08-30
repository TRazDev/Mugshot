package uk.co.fractalmotion.mugshot.preview.junit

import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.android.resources.LayoutDirection
import com.android.resources.NightMode
import com.android.resources.ScreenOrientation
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewConfig
import uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewDevice
import uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewRenderingMode

/**
 * Builds the rule for one generated preview case.
 *
 * This is the only place that can see both halves: `mugshot-preview-runtime` sits on the main
 * compile classpath and cannot reference `DeviceConfig`, while `mugshot` is a test-only dependency.
 *
 * A fresh rule per case is not an implementation detail — `supportsRtl` is a constructor argument
 * and `unsafeUpdateConfig` cannot change it, so a single shared rule could not render both
 * left-to-right and right-to-left.
 */
public fun MugshotPreviewConfig.newMugshot(): Mugshot =
  Mugshot(
    deviceConfig = deviceConfig(),
    renderingMode = renderingMode.toRenderingMode(),
    supportsRtl = rtl
  )

private fun MugshotPreviewConfig.deviceConfig(): DeviceConfig =
  device.toDeviceConfig().copy(
    nightMode = if (nightMode) NightMode.NIGHT else NightMode.NOTNIGHT,
    fontScale = fontScale,
    locale = locale,
    layoutDirection = if (rtl) LayoutDirection.RTL else LayoutDirection.LTR
  )

private fun MugshotPreviewDevice.toDeviceConfig(): DeviceConfig =
  when (this) {
    MugshotPreviewDevice.DEFAULT -> DeviceConfig.PIXEL_6
    MugshotPreviewDevice.PHONE -> DeviceConfig.PIXEL_6
    MugshotPreviewDevice.FOLDABLE -> DeviceConfig.PIXEL_FOLD
    MugshotPreviewDevice.TABLET -> DeviceConfig.PIXEL_TABLET
    MugshotPreviewDevice.LANDSCAPE ->
      DeviceConfig.PIXEL_6.copy(orientation = ScreenOrientation.LANDSCAPE)
    MugshotPreviewDevice.WEAR_ROUND -> DeviceConfig.WEAR_OS_SMALL_ROUND
    MugshotPreviewDevice.WEAR_SQUARE -> DeviceConfig.WEAR_OS_SQUARE
  }

private fun MugshotPreviewRenderingMode.toRenderingMode(): RenderingMode =
  when (this) {
    MugshotPreviewRenderingMode.NORMAL -> RenderingMode.NORMAL
    MugshotPreviewRenderingMode.SHRINK -> RenderingMode.SHRINK
    MugshotPreviewRenderingMode.V_SCROLL -> RenderingMode.V_SCROLL
  }
