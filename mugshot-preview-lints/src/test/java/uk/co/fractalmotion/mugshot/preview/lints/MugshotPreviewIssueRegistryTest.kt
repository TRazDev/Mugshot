package uk.co.fractalmotion.mugshot.preview.lints

import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Severity
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * The registry is what lint actually loads, so an issue the detector declares but the registry
 * omits is a check that silently never runs. [MugshotPreviewDetectorTest] covers what each issue
 * reports; this covers that they are all reachable.
 */
class MugshotPreviewIssueRegistryTest {
  private lateinit var registry: MugshotPreviewIssueRegistry

  @Before
  fun setUp() {
    // IssueRegistry's constructor refuses to run until lint knows who is driving it. The
    // detector tests get this from lint's own harness; a plain unit test has to say so.
    LintClient.clientName = LintClient.CLIENT_UNIT_TESTS
    registry = MugshotPreviewIssueRegistry()
  }

  @Test
  fun registersEveryIssueTheDetectorDeclares() {
    // Kotlin puts a companion property's backing field on the enclosing class as a static,
    // so this reads MugshotPreviewDetector rather than its Companion.
    val declared = MugshotPreviewDetector::class.java.declaredFields
      .filter { Modifier.isStatic(it.modifiers) && Issue::class.java.isAssignableFrom(it.type) }
      .map {
        it.isAccessible = true
        it.get(null) as Issue
      }

    assertThat(declared).isNotEmpty()
    assertThat(registry.issues).containsExactlyElementsIn(declared)
  }

  @Test
  fun everyIssueIsImplementedByTheDetector() {
    // An issue wired to another detector would never fire from this registry.
    assertThat(registry.issues.map { it.implementation.detectorClass })
      .containsExactly(*Array(registry.issues.size) { MugshotPreviewDetector::class.java })
  }

  @Test
  fun idsAreStableAndUnique() {
    // Ids appear in consumers' lint baselines and @Suppress annotations, so renaming one is a
    // breaking change for them. Pinning them here makes that deliberate rather than accidental.
    assertThat(registry.issues.map { it.id }).containsExactly(
      "ComposableAnnotationNotFound",
      "PreviewAnnotationNotFound",
      "PrivatePreviewDetected",
      "MugshotPreviewArgumentsIgnored"
    )
  }

  @Test
  fun onlyIgnoredPreviewArgumentsAreAWarning() {
    // The other three describe a preview that produces no golden at all, which is a build error.
    val bySeverity = registry.issues.associate { it.id to it.defaultSeverity }

    assertThat(bySeverity["MugshotPreviewArgumentsIgnored"]).isEqualTo(Severity.WARNING)
    assertThat(bySeverity["ComposableAnnotationNotFound"]).isEqualTo(Severity.ERROR)
    assertThat(bySeverity["PreviewAnnotationNotFound"]).isEqualTo(Severity.ERROR)
    assertThat(bySeverity["PrivatePreviewDetected"]).isEqualTo(Severity.ERROR)
  }

  @Test
  fun reportsAVendorSoUsersCanFileIssues() {
    assertThat(registry.vendor.vendorName).isEqualTo("TRazDev/Mugshot")
    assertThat(registry.vendor.feedbackUrl).isEqualTo("https://github.com/TRazDev/Mugshot/issues")
  }
}
