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
    val lightName =
      "uk.co.fractalmotion.mugshot.plugin.test_MugshotGeneratedPreviewTest_snapshot[HelloPreview_HelloPreview_Light].webp"
    val darkName =
      "uk.co.fractalmotion.mugshot.plugin.test_MugshotGeneratedPreviewTest_snapshot[HelloPreview_HelloPreview_Dark].webp"

    // Compared against the directory listing rather than with File.exists(). Generated case names
    // keep the source's capitalisation, and exists() matches case-insensitively on macOS and
    // Windows, so a wrong-case expectation passes there and fails only on Linux -- which is how
    // the lower-cased names this replaces reached CI.
    val imageNames = File(snapshotsDir, "images").list()?.toList().orEmpty()
    assertThat(imageNames).containsAtLeast(lightName, darkName)

    // Dark mode has to actually change the render.
    val light = File(snapshotsDir, "images/$lightName")
    val dark = File(snapshotsDir, "images/$darkName")
    assertThat(light.readBytes()).isNotEqualTo(dark.readBytes())
  }
}
