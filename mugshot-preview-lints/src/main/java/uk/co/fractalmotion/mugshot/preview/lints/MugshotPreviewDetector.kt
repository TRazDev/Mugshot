package uk.co.fractalmotion.mugshot.preview.lints

import com.android.tools.lint.detector.api.AnnotationInfo
import com.android.tools.lint.detector.api.AnnotationUsageInfo
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.AnnotationUsageType.DEFINITION
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiAnnotation
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastVisibility

public class MugshotPreviewDetector : Detector(), SourceCodeScanner {
  override fun applicableAnnotations(): List<String> = listOf(MUGSHOT_ANNOTATION)

  override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean =
    type == DEFINITION || super.isApplicableAnnotationUsage(type)

  override fun inheritAnnotation(annotation: String): Boolean = false

  @Suppress("UnstableApiUsage")
  override fun visitAnnotationUsage(
    context: JavaContext,
    element: UElement,
    annotationInfo: AnnotationInfo,
    usageInfo: AnnotationUsageInfo
  ) {
    val qualifiedName = annotationInfo.qualifiedName
    if (qualifiedName != MUGSHOT_ANNOTATION) return

    val annotatedMethod = annotationInfo.annotation.uastParent as? UMethod
      ?: throw IllegalStateException("Expected annotated method given declared target type")

    val annotatedMethodName = annotatedMethod.name
    val hasComposable = annotatedMethod.uAnnotations.any { it.qualifiedName == COMPOSABLE_ANNOTATION }
    if (!hasComposable) {
      context.report(
        issue = COMPOSABLE_NOT_DETECTED,
        scope = element,
        location = context.getLocation(element),
        message = "$annotatedMethodName is not annotated with @Composable"
      )
    }

    if (!context.containsPreview(annotatedMethod.uAnnotations)) {
      context.report(
        issue = PREVIEW_NOT_DETECTED,
        scope = element,
        location = context.getLocation(element),
        message = "$annotatedMethodName is not annotated with @Preview"
      )
    }

    val ignoredArguments = annotatedMethod.uAnnotations
      .filter { it.qualifiedName == PREVIEW_ANNOTATION }
      .flatMap { it.attributeValues }
      .mapNotNull { it.name }
      .filter { it in IGNORED_PREVIEW_ARGUMENTS }
      .distinct()
      .sorted()
    if (ignoredArguments.isNotEmpty()) {
      context.report(
        issue = PREVIEW_ARGUMENTS_IGNORED,
        scope = element,
        location = context.getLocation(element),
        message = "@Preview of $annotatedMethodName sets ${ignoredArguments.joinToString()}, which " +
          "Mugshot ignores. Configure the snapshot with the Mugshot annotations instead."
      )
    }

    if (annotatedMethod.visibility == UastVisibility.PRIVATE) {
      context.report(
        issue = PRIVATE_PREVIEW_DETECTED,
        scope = element,
        location = context.getLocation(element),
        message = "$annotatedMethodName is private. Make it internal or public to generate a snapshot."
      )
    }
  }

  /**
   * Reports whether `@Preview` is reachable, directly or through a multi-preview annotation.
   *
   * The processor resolves `@Preview` recursively, so this has to as well — checking only direct
   * annotations would reject a function the processor is perfectly happy to generate from. The
   * visited set breaks the cycle an annotation applied to itself would otherwise create.
   */
  private fun JavaContext.containsPreview(
    annotations: List<UAnnotation>,
    visited: MutableSet<String> = mutableSetOf()
  ): Boolean =
    annotations.any { annotation ->
      val qualifiedName = annotation.qualifiedName
      when {
        qualifiedName == null -> false
        qualifiedName == PREVIEW_ANNOTATION -> true
        !visited.add(qualifiedName) -> false
        else -> {
          val declaration = evaluator.findClass(qualifiedName)
          declaration != null && containsPreview(declaration.annotations, visited)
        }
      }
    }

  private fun JavaContext.containsPreview(
    annotations: Array<out PsiAnnotation>,
    visited: MutableSet<String> = mutableSetOf()
  ): Boolean =
    annotations.any { annotation ->
      val qualifiedName = annotation.qualifiedName
      when {
        qualifiedName == null -> false
        qualifiedName == PREVIEW_ANNOTATION -> true
        !visited.add(qualifiedName) -> false
        else -> {
          val declaration = evaluator.findClass(qualifiedName)
          declaration != null && containsPreview(declaration.annotations, visited)
        }
      }
    }

  internal companion object {
    private const val MUGSHOT_ANNOTATION = "uk.co.fractalmotion.mugshot.annotations.Mugshot"
    private const val COMPOSABLE_ANNOTATION = "androidx.compose.runtime.Composable"
    private const val PREVIEW_ANNOTATION = "androidx.compose.ui.tooling.preview.Preview"

    val COMPOSABLE_NOT_DETECTED: Issue = Issue.create(
      id = "ComposableAnnotationNotFound",
      briefDescription = "Composable Annotation not found",
      explanation = "Mugshot Previews require a @Composable annotation to be applied.",
      category = Category.CUSTOM_LINT_CHECKS,
      priority = 10,
      severity = Severity.ERROR,
      implementation = Implementation(
        MugshotPreviewDetector::class.java,
        Scope.JAVA_FILE_SCOPE,
        Scope.JAVA_FILE_SCOPE
      )
    )

    val PREVIEW_NOT_DETECTED: Issue = Issue.create(
      id = "PreviewAnnotationNotFound",
      briefDescription = "Preview Annotation not found",
      explanation = "Mugshot Previews require a @Preview annotation to be applied.",
      category = Category.CUSTOM_LINT_CHECKS,
      priority = 10,
      severity = Severity.ERROR,
      implementation = Implementation(
        MugshotPreviewDetector::class.java,
        Scope.JAVA_FILE_SCOPE,
        Scope.JAVA_FILE_SCOPE
      )
    )

    /**
     * `@Preview` arguments Mugshot does not read.
     *
     * `name` and `group` are absent on purpose: they affect only how the IDE lists a preview, so
     * setting them says nothing about what the golden will contain.
     */
    private val IGNORED_PREVIEW_ARGUMENTS = setOf(
      "apiLevel",
      "backgroundColor",
      "device",
      "fontScale",
      "heightDp",
      "locale",
      "showBackground",
      "showSystemUi",
      "uiMode",
      "wallpaper",
      "widthDp"
    )

    val PREVIEW_ARGUMENTS_IGNORED: Issue = Issue.create(
      id = "MugshotPreviewArgumentsIgnored",
      briefDescription = "@Preview arguments are ignored by Mugshot",
      explanation = "Mugshot takes its configuration from the @Mugshot annotations rather than " +
        "from @Preview, so configuration set on @Preview changes what the IDE renders without " +
        "changing the golden image. Use `@MugshotDevices`, `@MugshotLightDark`, " +
        "`@MugshotFontScales`, `@MugshotLocales`, `@MugshotShrink` or `@MugshotFullScreen` instead.",
      category = Category.CUSTOM_LINT_CHECKS,
      priority = 5,
      severity = Severity.WARNING,
      implementation = Implementation(
        MugshotPreviewDetector::class.java,
        Scope.JAVA_FILE_SCOPE,
        Scope.JAVA_FILE_SCOPE
      )
    )

    val PRIVATE_PREVIEW_DETECTED: Issue = Issue.create(
      id = "PrivatePreviewDetected",
      briefDescription = "@Preview of private Composable detected",
      explanation = "Mugshot Previews does not support private Composables.",
      category = Category.CUSTOM_LINT_CHECKS,
      priority = 10,
      severity = Severity.ERROR,
      implementation = Implementation(
        MugshotPreviewDetector::class.java,
        Scope.JAVA_FILE_SCOPE,
        Scope.JAVA_FILE_SCOPE
      )
    )
  }
}
