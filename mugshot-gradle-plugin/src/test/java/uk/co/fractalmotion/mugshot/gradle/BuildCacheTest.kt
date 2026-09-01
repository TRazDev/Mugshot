package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.junit.Test
import java.io.File

/**
 * Up-to-date checks, the build cache and the configuration cache.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class BuildCacheTest : MugshotPluginTestCase() {
  @Test
  fun prepareResourcesCaching() {
    val fixtureRoot = fixture("prepare-resources-task-caching")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = fixtureRoot.runBuild("testRelease", "testDebug", "--build-cache")

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotDebugResources", FROM_CACHE)

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotReleaseResources", FROM_CACHE)

    var resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()
    var resourceFileContents = resourcesFile.readLines()
    assertThat(resourceFileContents.any { it.contains("release") }).isFalse()

    resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/release/resources.json")
    assertThat(resourcesFile.exists()).isTrue()
    resourceFileContents = resourcesFile.readLines()
    assertThat(resourceFileContents.any { it.contains("debug") }).isFalse()

    // delete now (regardless of future cleanup)
    buildDir.deleteRecursively()

    val secondRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE)

    resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()
    resourceFileContents = resourcesFile.readLines()
    assertThat(resourceFileContents.any { it.contains("release") }).isFalse()
  }

  @Test
  fun cacheable() {
    val fixtureRoot = fixture("cacheable")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotDebugResources", FROM_CACHE)

    buildDir.deleteRecursively()

    val secondRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE)
  }

  @Test
  fun cacheableRelocatable() {
    val fixtureRoot = fixture("cacheable")
    fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val firstRun = fixtureRoot.runBuild("testDebug", "--build-cache")

    firstRun.assertTaskOutcomeIsNot(":prepareMugshotDebugResources", FROM_CACHE)
    firstRun.assertTaskOutcomeIsNot(":testDebugUnitTest", FROM_CACHE)

    // Rebuild the same project (with its populated cache) from a different directory, as CI and a
    // local clone would. Absolute paths in the cache key would make these entries unreachable here.
    val relocatedRoot = fixtureRoot.parentFile.resolve("cacheable-relocated").registerForDeletionOnExit()
    relocatedRoot.deleteRecursively()
    fixtureRoot.copyRecursively(relocatedRoot)
    relocatedRoot.resolve("build").deleteRecursively()
    // Pin the project name (fed into the Kotlin module name embedded in compiled classes) so it
    // stays constant across dirs; real CI-vs-local checkouts share the same leaf directory name.
    relocatedRoot.resolve("settings.gradle").let {
      it.writeText("rootProject.name = 'cacheable'\n${it.readText()}")
    }

    val secondRun = relocatedRoot.runBuild("testDebug", "--build-cache")

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE)
    secondRun.assertTaskOutcome(":testDebugUnitTest", FROM_CACHE)
  }

  @Test
  fun configurationCache() {
    val fixtureRoot = fixture("configuration-cache")

    // check to avoid plugin regressions that might affect Gradle's configuration caching
    // https://docs.gradle.org/current/userguide/configuration_cache.html
    fixtureRoot.runBuild("testDebug", "--configuration-cache")
  }

  @Test
  fun configurationCacheWorksWithGeneratedSources() {
    val fixtureRoot = fixture("configuration-cache-generated-sources")

    // check to avoid plugin regressions that might affect Gradle's configuration caching
    // https://docs.gradle.org/current/userguide/configuration_cache.html
    fixtureRoot.runBuild("testDebug", "--configuration-cache")
  }

  /**
   * The Mugshot tasks, rather than `testDebug`, under the configuration cache.
   *
   * `testDebug` is neither a record nor a verify run, so the snapshot directory -- resolved from
   * the unit test source set, which `generateMugshot<Variant>PreviewTests` contributes a generated
   * directory to -- is never read. Only the Mugshot tasks register it as a task input or output,
   * and serialising those properties resolves it before the generating task has run. Reaching for
   * the source set's `all` directories on that path fails with
   * "Querying the mapped value of and(property 'static', property 'all') before task ... has
   * completed is not supported".
   *
   * The fixture applies KSP, without which the plugin registers no generated-test task and there
   * is no producer to trip over.
   */
  @Test
  fun configurationCacheWorksWithMugshotTasks() {
    val fixtureRoot = fixture("configuration-cache-preview-tests")
    File(fixtureRoot, "src/test/snapshots").registerForDeletionOnExit()

    fixtureRoot.runBuild("recordMugshotDebug", "--configuration-cache")
    fixtureRoot.runBuild("verifyMugshotDebug", "--configuration-cache")
    fixtureRoot.runBuild("deleteDebugMugshotSnapshots", "--configuration-cache")
  }
}
