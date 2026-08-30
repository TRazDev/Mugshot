package uk.co.fractalmotion.mugshot.sample.component

import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.catalog.Appearance
import uk.co.fractalmotion.mugshot.sample.catalog.GalleryPage
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme

/**
 * Snapshots every design system page, light and dark.
 *
 * `RenderingMode.SHRINK` makes each golden wrap to its content instead of filling a device, which
 * keeps a gallery of specimens from carrying a screen's worth of empty background.
 */
@RunWith(TestParameterInjector::class)
class ComponentGallerySnapshotTest(
  @TestParameter private val page: GalleryPage,
  @TestParameter private val appearance: Appearance
) {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.PIXEL_6,
    renderingMode = RenderingMode.SHRINK
  )

  @Test
  fun gallery() {
    mugshot.snapshot {
      MugshotTheme(darkTheme = appearance.dark) { page.content() }
    }
  }
}
