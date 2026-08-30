package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Applying the plugin to the module types and test frameworks it supports.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class PluginApplicationTest : MugshotPluginTestCase() {
  @Test
  fun androidApplicationPlugin() {
    val fixtureRoot = File("src/test/projects/supports-application-modules")

    val result = gradleRunner
      .withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
    assertThat(result.task(":testDebugUnitTest")).isNotNull()
  }

  @Test
  fun androidDynamicFeaturePlugin() {
    val fixtureRoot = File("src/test/projects/supports-dynamic-feature-modules")

    val result = gradleRunner
      .withArguments(":dynamic_feature:verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":dynamic_feature:prepareMugshotDebugResources")).isNotNull()
    assertThat(result.task(":dynamic_feature:testDebugUnitTest")).isNotNull()
  }

  @Test
  fun supportsJunitJupiterLibrary() {
    val fixtureRoot = File("src/test/projects/supports-junit-jupiter")

    gradleRunner.withArguments("verifyMugshotDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun missingSupportedPlugins() {
    val fixtureRoot = File("src/test/projects/missing-supported-plugins")

    val result = gradleRunner
      .withArguments("prepareMugshotDebugResources", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNull()
    assertThat(result.output).contains(
      "One of com.android.application, com.android.library, com.android.dynamic-feature, com.android.kotlin.multiplatform.library must be applied for Mugshot to work properly."
    )
  }

  @Test
  fun declareAndroidPluginAfter() {
    val fixtureRoot = File("src/test/projects/declare-android-plugin-after")

    val result = gradleRunner
      .withArguments("prepareMugshotDebugResources", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
  }

  @Test
  fun kotlinMultiplatformPluginWithNewAndroidLibraryPlugin() {
    val fixtureRoot = File("src/test/projects/multiplatform-android-plugin")

    val result = gradleRunner
      .withArguments("verifyMugshotAndroidMain", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":prepareMugshotAndroidMainResources")).isNotNull()
  }

  @Test
  fun erroneouslyConfiguredInCommonTest() {
    val fixtureRoot = File("src/test/projects/multiplatform-mugshot-in-commontest")

    val result = gradleRunner
      .withArguments("prepareMugshotDebugResources", "--stacktrace")
      .runFixture(fixtureRoot) { buildAndFail() }

    assertThat(result.output).contains(
      "Mugshot must not be declared in 'commonTestImplementation', as it should only resolve on Android JVM tests."
    )
  }

  @Test
  fun excludeAndroidTestSourceSets() {
    val fixtureRoot = File("src/test/projects/exclude-androidtest")

    val result = gradleRunner
      .withArguments("prepareMugshotDebugResources", "--stacktrace")
      .runFixture(fixtureRoot) { build() }

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
  }

  @Test
  fun disabledUnitTestVariant() {
    val fixtureRoot = File("src/test/projects/disabled-unit-test-variant")
    gradleRunner
      .withArguments("testDebug")
      .runFixture(fixtureRoot) { build() }
  }

  @Test
  fun robolectric() {
    val fixtureRoot = File("src/test/projects/robolectric")

    gradleRunner
      .withArguments("testDebug", "--stacktrace")
      .runFixture(fixtureRoot) { build() }
  }
}
