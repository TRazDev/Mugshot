package uk.co.fractalmotion.mugshot.gradle

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory

/**
 * Reports a preview by name.
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
 * MugshotGeneratedPreviewTest > designsystem.catalog.MugshotChipCatalog.MugshotChipCatalogPreview [Light] PASSED
 * ```
 *
 * A test that is not a generated preview keeps its own name.
 */
internal class ReadableTestNameListener(
  private val generatedResources: Provider<Directory>,
  private val outputFactory: StyledTextOutputFactory
) : TestListener {
  private val displayNames: Map<String, String> by lazy { readDisplayNames() }

  override fun beforeSuite(suite: TestDescriptor): Unit = Unit

  override fun afterSuite(suite: TestDescriptor, result: TestResult): Unit = Unit

  override fun beforeTest(testDescriptor: TestDescriptor): Unit = Unit

  override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
    val output = outputFactory.create(ReadableTestNameListener::class.java)
    output.println()
    output.text(displayNameOf(testDescriptor))
    output.text(" ")

    // The same words and colours Gradle's own reporting uses, so a build that turns this on does
    // not have to learn a second vocabulary.
    when (result.resultType) {
      TestResult.ResultType.SUCCESS -> output.withStyle(StyledTextOutput.Style.SuccessHeader).text("PASSED")
      TestResult.ResultType.SKIPPED -> output.withStyle(StyledTextOutput.Style.Info).text("SKIPPED")
      TestResult.ResultType.FAILURE -> output.withStyle(StyledTextOutput.Style.FailureHeader).text("FAILED")
    }
    output.println()

    // Gradle prints the exception under a failure, and this replaces Gradle's line, so it has to
    // print it too. Indented four, which is where Gradle puts it.
    result.exceptions.forEach { failure ->
      output.withStyle(StyledTextOutput.Style.Failure)
        .println(failure.toString().prependIndent("    "))
    }
  }

  private fun displayNameOf(descriptor: TestDescriptor): String {
    val className = descriptor.className?.substringAfterLast('.').orEmpty()
    val snapshotName = descriptor.name.substringAfter("snapshot[", "").substringBeforeLast("]", "")
    val name = displayNames[snapshotName] ?: descriptor.name
    return if (className.isEmpty()) name else "$className > $name"
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
