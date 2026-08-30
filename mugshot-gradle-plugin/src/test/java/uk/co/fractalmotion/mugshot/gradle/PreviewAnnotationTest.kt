package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Screenshot coverage from annotations alone.
 *
 * The fixture has no test source set at all: the plugin supplies the annotations, the preview
 * runtime and the KSP processor, and generates the JUnit test that renders them.
 */
@Suppress("ktlint:standard:max-line-length")
class PreviewAnnotationTest : MugshotPluginTestCase() {
  @Test
  fun recordsOneGoldenPerConfiguration() {
    val fixtureRoot = fixture("preview-annotations")
    val snapshotsDir = File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    fixtureRoot.runBuild("recordMugshotDebug")

    // @MugshotLightDark asks for two configurations, so one preview yields two goldens.
    val light = File(
      snapshotsDir,
      "images/uk.co.fractalmotion.mugshot.plugin.test_MugshotGeneratedPreviewTest_snapshot[hellopreview_hellopreview_light].webp"
    )
    val dark = File(
      snapshotsDir,
      "images/uk.co.fractalmotion.mugshot.plugin.test_MugshotGeneratedPreviewTest_snapshot[hellopreview_hellopreview_dark].webp"
    )

    assertThat(light.exists()).isTrue()
    assertThat(dark.exists()).isTrue()
    // Dark mode has to actually change the render.
    assertThat(light.readBytes()).isNotEqualTo(dark.readBytes())
  }
}
