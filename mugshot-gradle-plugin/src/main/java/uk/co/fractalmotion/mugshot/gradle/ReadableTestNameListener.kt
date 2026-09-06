package uk.co.fractalmotion.mugshot.gradle

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

/**
 * Reports a passing preview by name.
 *
 * Gradle names a generated preview test after the golden image, which is a flat string repeated
 * twice in the same line:
 *
 * ```
 * MugshotGeneratedPreviewTest > [designsystem_catalog_MugshotChipCatalog_MugshotChipCatalogPreview_Light] > snapshot[designsystem_catalog_MugshotChipCatalog_MugshotChipCatalogPreview_Light] PASSED
 * ```
 *
 * The name is flat because it becomes a filename, and it cannot be unflattened here: only the
 * annotation processor knows which underscores separate the path from the function from the axes.
 * So the processor writes the pairing out, and this reads it back:
 *
 * ```
 * designsystem.catalog.MugshotChipCatalog.MugshotChipCatalogPreview [Light] PASSED
 * ```
 *
 * Failures are left to Gradle, which already prints them with the assertion that explains them.
 * A test that is not a generated preview keeps its own name.
 */
internal class ReadableTestNameListener(
  private val generatedResources: Provider<Directory>
) : TestListener {
  private val displayNames: Map<String, String> by lazy { readDisplayNames() }

  override fun beforeSuite(suite: TestDescriptor): Unit = Unit

  override fun afterSuite(suite: TestDescriptor, result: TestResult): Unit = Unit

  override fun beforeTest(testDescriptor: TestDescriptor): Unit = Unit

  override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
    val outcome = when (result.resultType) {
      // Gradle's own lines read PASSED rather than SUCCESS, and this replaces those.
      TestResult.ResultType.SUCCESS -> "PASSED"
      TestResult.ResultType.SKIPPED -> "SKIPPED"
      TestResult.ResultType.FAILURE -> return
    }
    println("${displayNameOf(testDescriptor)} $outcome")
  }

  private fun displayNameOf(descriptor: TestDescriptor): String {
    val snapshotName = descriptor.name.substringAfter("snapshot[", "").substringBeforeLast("]", "")
    displayNames[snapshotName]?.let { return it }
    val className = descriptor.className?.substringAfterLast('.').orEmpty()
    return if (className.isEmpty()) descriptor.name else "$className.${descriptor.name}"
  }

  /**
   * Read once, on the first test to finish, rather than when the listener is built: the processor
   * writes the file during the compilation this task depends on, which has not run yet.
   */
  private fun readDisplayNames(): Map<String, String> {
    val root = generatedResources.orNull?.asFile ?: return emptyMap()
    if (!root.isDirectory) return emptyMap()
    val file = root.walkTopDown().firstOrNull { it.name == PREVIEW_NAMES_FILE } ?: return emptyMap()
    return file.readLines()
      .mapNotNull { line ->
        val separator = line.indexOf('\t')
        if (separator <= 0) null else line.substring(0, separator) to line.substring(separator + 1)
      }
      .toMap()
  }

  private companion object {
    /** Written by the preview processor. Keep the name in step there. */
    private const val PREVIEW_NAMES_FILE = "mugshotPreviewNames.txt"
  }
}
