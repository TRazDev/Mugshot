package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Correspondence
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.buffer
import okio.source
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.After
import org.junit.Before
import uk.co.fractalmotion.mugshot.gradle.PrepareResourcesTask.Config
import java.io.File

/**
 * Shared machinery for the Gradle plugin's TestKit suites.
 *
 * Every test drives a nested Gradle build against a fixture project under `src/test/projects/`.
 * The suites are split across several classes so Gradle can shard them across parallel forks --
 * [maxParallelForks][org.gradle.api.tasks.testing.Test.setMaxParallelForks] distributes work by
 * class, so a single class would pin the whole suite to one fork.
 *
 * Tests that share a fixture directory must live in the same class, or parallel forks race on
 * that directory.
 */
@Suppress("ktlint:standard:max-line-length")
abstract class MugshotPluginTestCase {
  private val filesToDelete = mutableListOf<File>()
  private val filesToRestore = mutableMapOf<File, ByteArray>()

  protected lateinit var gradleRunner: GradleRunner

  @Before
  fun setUpGradleRunner() {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
  }

  @After
  fun restoreFixtures() {
    filesToDelete.forEach(File::deleteRecursively)
    filesToRestore.forEach { (file, contents) -> file.writeBytes(contents) }
  }

  // `internal` rather than `protected`: PrepareResourcesTask.Config is itself internal.
  internal fun File.loadConfig() = source().buffer().use { CONFIG_ADAPTER.fromJson(it)!! }

  protected fun GradleRunner.runFixture(projectRoot: File, action: GradleRunner.() -> BuildResult): BuildResult {
    val settings = File(projectRoot, "settings.gradle")
    val gradleProperties = File(projectRoot, "gradle.properties")
    var generatedSettings = false
    var generatedGradleProperties = false

    return try {
      if (!settings.exists()) {
        settings.createNewFile()
        settings.writeText("apply from: \"../test.settings.gradle\"")
        generatedSettings = true
      }

      if (!gradleProperties.exists()) {
        gradleProperties.createNewFile()
        gradleProperties.writeText(
          """
            |android.dependencyResolutionAtConfigurationTime.disallow=true
          """.trimMargin()
        )
        generatedGradleProperties = true
      }

      withProjectDir(projectRoot).action()
    } finally {
      if (generatedSettings) settings.delete()
      if (generatedGradleProperties) gradleProperties.delete()
    }
  }

  protected fun File.registerForDeletionOnExit() = apply { filesToDelete += this }

  // A tracked fixture file that a test overwrites. Restoring it in tearDown keeps a test run
  // from leaving the working tree dirty.
  protected fun File.registerForRestoreOnExit() = apply { filesToRestore[this] = readBytes() }

  // Fixture projects keep their own build/ and .gradle/ between runs, and git does not see them.
  // A test asserting that a nested build re-ran has to clear that state first, otherwise the
  // build is simply up-to-date and rewrites nothing.
  protected fun File.clearNestedBuildState() =
    apply {
      File(this, "build").deleteRecursively()
      File(this, ".gradle").deleteRecursively()
    }

  protected fun File.listFilesSorted() = listFiles()?.sortedBy { it.lastModified() }

  companion object {
    internal const val GRADLE_CACHE_TRANSFORMS_PATH_REGEX = "^caches/[0-9]{1,2}.[0-9]{1,2}(.[0-9])?(-rc-[0-9]{1,2})?/transforms/[0-9a-f]{32}/(workspace/)?transformed"

    internal val CONFIG_ADAPTER =
      Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()!!.adapter(Config::class.java)
    internal val MATCHES_PATTERN = Correspondence.from<String, String>(
      { actual, expected -> actual.matches(expected.toRegex()) }, "matches"
    )
  }
}
