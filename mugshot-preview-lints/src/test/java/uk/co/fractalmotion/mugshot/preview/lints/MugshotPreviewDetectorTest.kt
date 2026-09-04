package uk.co.fractalmotion.mugshot.preview.lints

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import org.junit.Test

class MugshotPreviewDetectorTest {
  @Test
  fun simplePreview() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Preview
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .detector(MugshotPreviewDetector())
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expectClean()
  }

  @Test
  fun multiplePreviews() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Preview
          @Preview(
             name = "Night Pixel 4",
             uiMode = 0x20, // uiMode maps to android.content.res.Configuration.UI_MODE_NIGHT_YES
             device = "id:pixel_4"
          )
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      // Scoped to the error checks: these previews differ by uiMode and device, which correctly
      // trips MugshotPreviewArgumentsIgnored -- covered by its own test.
      .issues(
        MugshotPreviewDetector.COMPOSABLE_NOT_DETECTED,
        MugshotPreviewDetector.PREVIEW_NOT_DETECTED,
        MugshotPreviewDetector.PRIVATE_PREVIEW_DETECTED
      )
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expectClean()
  }

  @Test
  fun notComposable() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Preview
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .issues(MugshotPreviewDetector.COMPOSABLE_NOT_DETECTED)
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expect(
        """
        src/test/test.kt:6: Error: SamplePreview is not annotated with @Composable [ComposableAnnotationNotFound]
        @Mugshot
        ~~~~~~~~
        1 errors, 0 warnings
        """.trimIndent()
      )
  }

  @Test
  fun notPreview() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .issues(MugshotPreviewDetector.PREVIEW_NOT_DETECTED)
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expect(
        """
        src/test/test.kt:6: Error: SamplePreview is not annotated with @Preview [PreviewAnnotationNotFound]
        @Mugshot
        ~~~~~~~~
        1 errors, 0 warnings
        """.trimIndent()
      )
  }

  @Test
  fun privatePreview() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Preview
          @Composable
          private fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .issues(MugshotPreviewDetector.PRIVATE_PREVIEW_DETECTED)
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expect(
        """
        src/test/test.kt:7: Error: SamplePreview is private. Make it internal or public to generate a snapshot. [PrivatePreviewDetected]
        @Mugshot
        ~~~~~~~~
        1 errors, 0 warnings
        """.trimIndent()
      )
  }

  @Test
  fun previewParameters() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import androidx.compose.ui.tooling.preview.PreviewParameter
          import androidx.compose.ui.tooling.preview.PreviewParameterProvider
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Preview
          @Composable
          fun SamplePreview(
            @PreviewParameter(SamplePreviewParameter::class) text: String,
          ) {}

          object SamplePreviewParameter: PreviewParameterProvider<String> {
            override val values: Sequence<String> = sequenceOf("test")
          }
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .detector(MugshotPreviewDetector())
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expectClean()
  }

  @Test
  fun transitivePreview() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Preview(name = "Light")
          @Preview(name = "Dark", uiMode = 0x20)
          annotation class ThemePreviews

          @Mugshot
          @ThemePreviews
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .detector(MugshotPreviewDetector())
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expectClean()
  }

  @Test
  fun selfReferencingAnnotationDoesNotRecurse() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Cyclic
          annotation class Cyclic

          @Mugshot
          @Cyclic
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .issues(MugshotPreviewDetector.PREVIEW_NOT_DETECTED)
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expect(
        """
        src/test/Cyclic.kt:9: Error: SamplePreview is not annotated with @Preview [PreviewAnnotationNotFound]
        @Mugshot
        ~~~~~~~~
        1 errors, 0 warnings
        """.trimIndent()
      )
  }

  @Test
  fun previewArgumentsAreReportedAsIgnored() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Preview(name = "Big", fontScale = 2f, device = "id:pixel_5")
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .issues(MugshotPreviewDetector.PREVIEW_ARGUMENTS_IGNORED)
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expect(
        """
        src/test/test.kt:7: Warning: @Preview of SamplePreview sets device, fontScale, which Mugshot ignores. Configure the snapshot with the Mugshot annotations instead. [MugshotPreviewArgumentsIgnored]
        @Mugshot
        ~~~~~~~~
        0 errors, 1 warnings
        """.trimIndent()
      )
  }

  @Test
  fun previewNameAndGroupAreNotReported() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Mugshot
          @Preview(name = "Anything", group = "Screens")
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .issues(MugshotPreviewDetector.PREVIEW_ARGUMENTS_IGNORED)
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expectClean()
  }

  private companion object {
    private val COMPOSE_SOURCES =
      listOf(
        kotlin(
          """
          package androidx.compose.runtime

          @Retention(AnnotationRetention.BINARY)
          @Target(
              AnnotationTarget.FUNCTION,
              AnnotationTarget.TYPE,
              AnnotationTarget.TYPE_PARAMETER,
              AnnotationTarget.PROPERTY_GETTER
          )
          annotation class Composable
          """
        ).indented(),
        kotlin(
          """
          package androidx.compose.ui.tooling.preview

          @Retention(AnnotationRetention.BINARY)
          @Target(
              AnnotationTarget.ANNOTATION_CLASS,
              AnnotationTarget.FUNCTION
          )
          @Repeatable
          annotation class Preview(
            val name: String = "",
            val group: String = "",
            val apiLevel: Int = -1,
            val widthDp: Int = -1,
            val heightDp: Int = -1,
            val locale: String = "",
            val fontScale: Float = 1f,
            val showSystemUi: Boolean = false,
            val showBackground: Boolean = false,
            val backgroundColor: Long = 0,
            val uiMode: Int = 0,
            val device: String = "",
            val wallpaper: Int = 0,
          )
          """
        ).indented(),
        kotlin(
          """
          package androidx.compose.ui.tooling.preview

          import kotlin.jvm.JvmDefaultWithCompatibility
          import kotlin.reflect.KClass

          @JvmDefaultWithCompatibility
          interface PreviewParameterProvider<T> {
              val values: Sequence<T>
              val count get() = values.count()
          }

          annotation class PreviewParameter(
              val provider: KClass<out PreviewParameterProvider<*>>,
              val limit: Int = Int.MAX_VALUE
          )
          """
        ).indented()
      )

    val MUGSHOT_ANNOTATION = kotlin(
      """
      package uk.co.fractalmotion.mugshot.annotations

      @Target(AnnotationTarget.FUNCTION)
      @Retention(AnnotationRetention.BINARY)
      annotation class Mugshot
      """
    ).indented()
  }

  @Test
  fun `a source retained multi preview is followed`() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          @Retention(AnnotationRetention.SOURCE)
          @Preview
          annotation class SourceRetainedPreviews

          @Mugshot
          @SourceRetainedPreviews
          @Composable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .detector(MugshotPreviewDetector())
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expectClean()
  }

  @Test
  fun `a typealiased Composable is recognised`() {
    lint()
      .files(
        kotlin(
          """
          package test

          import androidx.compose.runtime.Composable
          import androidx.compose.ui.tooling.preview.Preview
          import uk.co.fractalmotion.mugshot.annotations.Mugshot

          typealias Drawable = Composable

          @Mugshot
          @Preview
          @Drawable
          fun SamplePreview() {}
          """
        ).indented(),
        *COMPOSE_SOURCES.toTypedArray(),
        MUGSHOT_ANNOTATION
      )
      .detector(MugshotPreviewDetector())
      .skipTestModes(TestMode.SUPPRESSIBLE)
      .run()
      .expectClean()
  }
}
