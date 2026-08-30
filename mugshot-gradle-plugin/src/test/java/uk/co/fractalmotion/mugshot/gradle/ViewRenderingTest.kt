package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Rendering regressions for the View toolkit. Each test passes when the golden matches.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class ViewRenderingTest : MugshotPluginTestCase() {
  @Test
  fun widgets() = fixture("widgets").verifyDebug()

  @Test
  fun ninePatch() = fixture("nine-patch").verifyDebug()

  @Test
  fun customFonts() = fixture("custom-fonts").verifyDebug()

  @Test
  fun textAppearances() = fixture("text-appearances").verifyDebug()

  @Test
  fun verifyVectorDrawables() = fixture("verify-svgs").verifyDebug()

  @Test
  fun verifyRecyclerView() = fixture("verify-recyclerview").verifyDebug()

  @Test
  fun verifyAaptAttrResourceParsing() = fixture("verify-aapt").verifyDebug()

  @Test
  fun verifySnapshot() {
    val fixtureRoot = fixture("verify-snapshot")

    val result = fixtureRoot.runBuild("verifyMugshotDebug")

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
    assertThat(result.task(":testDebugUnitTest")).isNotNull()
  }

  @Test
  fun withoutAppCompat() = fixture("appcompat-missing").verifyDebug()

  @Test
  fun withAppCompat() = fixture("appcompat-present").verifyDebug()

  @Test
  fun interceptViewEditMode() = fixture("edit-mode-intercept").buildSucceeds("testDebug")

  @Test
  fun lifecycleOwnerUsages() {
    val fixtureRoot = fixture("lifecycle-usages")

    fixtureRoot.runBuild("testDebug")

    val snapshotsDir = File(fixtureRoot, "build/reports/mugshot/debug/images")
    val snapshots = snapshotsDir.listFilesSorted()
    assertThat(snapshots!!).hasSize(3)
  }
}
