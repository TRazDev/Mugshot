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
    val fixtureRoot = fixture("supports-application-modules")

    val result = fixtureRoot.runBuild("verifyMugshotDebug")

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
    assertThat(result.task(":testDebugUnitTest")).isNotNull()
  }

  @Test
  fun androidDynamicFeaturePlugin() {
    val fixtureRoot = fixture("supports-dynamic-feature-modules")

    val result = fixtureRoot.runBuild(":dynamic_feature:verifyMugshotDebug")

    assertThat(result.task(":dynamic_feature:prepareMugshotDebugResources")).isNotNull()
    assertThat(result.task(":dynamic_feature:testDebugUnitTest")).isNotNull()
  }

  @Test
  fun supportsJunitJupiterLibrary() {
    val fixtureRoot = fixture("supports-junit-jupiter")

    fixtureRoot.runBuild("verifyMugshotDebug")
  }

  @Test
  fun missingSupportedPlugins() {
    val fixtureRoot = fixture("missing-supported-plugins")

    val result = fixtureRoot.runBuildAndFail("prepareMugshotDebugResources")

    assertThat(result.task(":prepareMugshotDebugResources")).isNull()
    assertThat(result.output).contains(
      "One of com.android.application, com.android.library, com.android.dynamic-feature, com.android.kotlin.multiplatform.library must be applied for Mugshot to work properly."
    )
  }

  @Test
  fun declareAndroidPluginAfter() {
    val fixtureRoot = fixture("declare-android-plugin-after")

    val result = fixtureRoot.runBuild("prepareMugshotDebugResources")

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
  }

  @Test
  fun kotlinMultiplatformPluginWithNewAndroidLibraryPlugin() {
    val fixtureRoot = fixture("multiplatform-android-plugin")

    val result = fixtureRoot.runBuild("verifyMugshotAndroidMain")

    assertThat(result.task(":prepareMugshotAndroidMainResources")).isNotNull()
  }

  @Test
  fun erroneouslyConfiguredInCommonTest() {
    val fixtureRoot = fixture("multiplatform-mugshot-in-commontest")

    val result = fixtureRoot.runBuildAndFail("prepareMugshotDebugResources")

    assertThat(result.output).contains(
      "Mugshot must not be declared in 'commonTestImplementation', as it should only resolve on Android JVM tests."
    )
  }

  @Test
  fun excludeAndroidTestSourceSets() {
    val fixtureRoot = fixture("exclude-androidtest")

    val result = fixtureRoot.runBuild("prepareMugshotDebugResources")

    assertThat(result.task(":prepareMugshotDebugResources")).isNotNull()
  }

  @Test
  fun disabledUnitTestVariant() {
    val fixtureRoot = fixture("disabled-unit-test-variant")
    fixtureRoot.runBuild("testDebug")
  }

  @Test
  fun robolectric() = fixture("robolectric").buildSucceeds("testDebug")
}
