package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
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
  fun widgets() {
    val fixtureRoot = File("src/test/projects/widgets")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun ninePatch() {
    val fixtureRoot = File("src/test/projects/nine-patch")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun customFonts() {
    val fixtureRoot = File("src/test/projects/custom-fonts")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun textAppearances() {
    val fixtureRoot = File("src/test/projects/text-appearances")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun verifyVectorDrawables() {
    val fixtureRoot = File("src/test/projects/verify-svgs")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun verifyRecyclerView() {
    val fixtureRoot = File("src/test/projects/verify-recyclerview")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun verifyAaptAttrResourceParsing() {
    val fixtureRoot = File("src/test/projects/verify-aapt")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun verifySnapshot() {
    val fixtureRoot = File("src/test/projects/verify-snapshot")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
    assertThat(result.task(":testDebugUnitTest")).isNotNull()
  }

  @Test
  fun withoutAppCompat() {
    val fixtureRoot = File("src/test/projects/appcompat-missing")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun withAppCompat() {
    val fixtureRoot = File("src/test/projects/appcompat-present")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  @Ignore
  fun withMaterialComponents() {
    val fixtureRoot = File("src/test/projects/material-components-present")

    gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun interceptViewEditMode() {
    val fixtureRoot = File("src/test/projects/edit-mode-intercept")

    gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun lifecycleOwnerUsages() {
    val fixtureRoot = File("src/test/projects/lifecycle-usages")

    gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    val snapshotsDir = File(fixtureRoot, "build/reports/mugshot/debug/images")
    val snapshots = snapshotsDir.listFilesSorted()
    assertThat(snapshots!!).hasSize(3)
  }
}
