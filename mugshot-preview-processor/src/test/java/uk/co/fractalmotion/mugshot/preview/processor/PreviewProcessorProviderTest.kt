@file:OptIn(ExperimentalCompilerApi::class)

package uk.co.fractalmotion.mugshot.preview.processor

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspProcessorOptions
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PreviewProcessorProviderTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  private val previewsFile: File
    get() = temporaryFolder.root.resolve("debug/ksp/sources/kotlin/$TEST_NAMESPACE/MugshotPreviews.kt")

  /** Snapshot names in the order they were generated. */
  private fun snapshotNames(): List<String> =
    Regex("""snapshotName = "([^"]+)"""")
      .findAll(previewsFile.readText())
      .map { it.groupValues[1] }
      .toList()

  @Test
  fun noAnnotatedPreviews() {
    val result = compile(
      """
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    // Still emitted: the generated test reads this list, and must compile in a module that has
    // not annotated anything yet.
    assertThat(snapshotNames()).isEmpty()
  }

  @Test
  fun simplePreview() {
    val result = compile(
      """
      @Mugshot
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(previewsFile.readText().trim()).isEqualTo(
      """
      package test

      internal val mugshotPreviewCases: List<uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewCase> = buildList {
        add(
          uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewCase(
            snapshotName = "SamplePreview_SamplePreview",
            config = uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewConfig(
              device = uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewDevice.DEFAULT,
              nightMode = false,
              fontScale = 1.0f,
              locale = null,
              rtl = false,
              renderingMode = uk.co.fractalmotion.mugshot.preview.runtime.MugshotPreviewRenderingMode.NORMAL
            ),
            frames = { listOf<@androidx.compose.runtime.Composable () -> Unit>({ test.SamplePreview() }) }
          )
        )
      }
      """.trimIndent()
    )
  }

  @Test
  fun multiplePreviewsStillProduceOneCase() {
    val result = compile(
      """
      @Mugshot
      @Preview
      @Preview(name = "Other", uiMode = 0x20)
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    // @Preview arguments are not the source of truth; the matrix comes from Mugshot annotations.
    assertThat(snapshotNames()).containsExactly("SamplePreview_SamplePreview")
  }

  @Test
  fun lightDark() {
    val result = compile(
      """
      @Mugshot
      @MugshotLightDark
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(snapshotNames()).containsExactly(
      "SamplePreview_SamplePreview_Light",
      "SamplePreview_SamplePreview_Dark"
    ).inOrder()
    assertThat(previewsFile.readText()).contains("nightMode = true")
  }

  @Test
  fun axesMultiply() {
    val result = compile(
      """
      @Mugshot
      @MugshotDevices(MugshotDevice.PHONE, MugshotDevice.TABLET)
      @MugshotLightDark
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(snapshotNames()).containsExactly(
      "SamplePreview_SamplePreview_Phone_Light",
      "SamplePreview_SamplePreview_Phone_Dark",
      "SamplePreview_SamplePreview_Tablet_Light",
      "SamplePreview_SamplePreview_Tablet_Dark"
    ).inOrder()
  }

  @Test
  fun matrixIsDevicesTimesLightDarkTimesFontScales() {
    val result = compile(
      """
      @Mugshot
      @MugshotMatrix
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(snapshotNames()).hasSize(24)
    assertThat(snapshotNames().first()).isEqualTo("SamplePreview_SamplePreview_Phone_Light_Font100")
    assertThat(snapshotNames().last()).isEqualTo("SamplePreview_SamplePreview_Landscape_Dark_Font200")
  }

  @Test
  fun localesKeepADefaultBaselineAndInferRtl() {
    val result = compile(
      """
      @Mugshot
      @MugshotLocales("ar", "en-rXA")
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(snapshotNames()).containsExactly(
      "SamplePreview_SamplePreview_Default",
      "SamplePreview_SamplePreview_ar",
      "SamplePreview_SamplePreview_en-rXA"
    ).inOrder()

    // Arabic is right to left; the accent pseudolocale is not.
    val generated = previewsFile.readText().replace(Regex("\\s+"), " ")
    assertThat(generated).contains("""locale = "ar", rtl = true,""")
    assertThat(generated).contains("""locale = "en-rXA", rtl = false,""")
  }

  @Test
  fun renderingModeAxes() {
    val shrink = compile(
      """
      @Mugshot
      @MugshotShrink
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )
    assertThat(shrink.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(previewsFile.readText()).contains("MugshotPreviewRenderingMode.SHRINK")
  }

  @Test
  fun fullScreenAxis() {
    val result = compile(
      """
      @Mugshot
      @MugshotFullScreen
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )
    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(previewsFile.readText()).contains("MugshotPreviewRenderingMode.V_SCROLL")
  }

  @Test
  fun wearAddsRoundAndSquare() {
    val result = compile(
      """
      @Mugshot
      @MugshotWear
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(snapshotNames()).containsExactly(
      "SamplePreview_SamplePreview_WearRound",
      "SamplePreview_SamplePreview_WearSquare"
    ).inOrder()
  }

  @Test
  fun bundleAnnotationIsFollowed() {
    val result = compile(
      """
      @Mugshot
      @MugshotLightDark
      annotation class OurScreenshots

      @OurScreenshots
      @Preview
      @Composable
      fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(snapshotNames()).containsExactly(
      "SamplePreview_SamplePreview_Light",
      "SamplePreview_SamplePreview_Dark"
    ).inOrder()
  }

  @Test
  fun privatePreview() {
    val result = compile(
      """
      @Mugshot
      @Preview
      @Composable
      private fun SamplePreview() = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    assertThat(result.messages)
      .contains("e: [ksp] test.SamplePreview is private. Make it internal or public to generate a snapshot.")
  }

  @Test
  fun unannotatedParameter() {
    val result = compile(
      """
      @Mugshot
      @Preview
      @Composable
      fun SamplePreview(text: String) = Unit
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    assertThat(result.messages)
      .contains("e: [ksp] test.SamplePreview has parameters. Only @PreviewParameter parameters are supported.")
  }

  @Test
  fun previewParametersFromObjectProvider() {
    val result = compile(
      """
      @Mugshot
      @Preview
      @Composable
      fun SamplePreview(
        @PreviewParameter(SampleProvider::class) text: String
      ) = Unit

      object SampleProvider : PreviewParameterProvider<String> {
        override val values: Sequence<String> = sequenceOf("a", "b")
      }
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(previewsFile.readText()).contains(
      "frames = { uk.co.fractalmotion.mugshot.preview.runtime.parameterizedFrames" +
        "(test.SampleProvider.values, 2147483647) { test.SamplePreview(it) } }"
    )
  }

  @Test
  fun previewParametersFromClassProviderWithLimit() {
    val result = compile(
      """
      @Mugshot
      @Preview
      @Composable
      fun SamplePreview(
        @PreviewParameter(SampleProvider::class, limit = 2) text: String
      ) = Unit

      class SampleProvider : PreviewParameterProvider<String> {
        override val values: Sequence<String> = sequenceOf("a", "b", "c")
      }
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(previewsFile.readText()).contains(
      "frames = { uk.co.fractalmotion.mugshot.preview.runtime.parameterizedFrames" +
        "(test.SampleProvider().values, 2) { test.SamplePreview(it) } }"
    )
  }

  @Test
  fun previewParameterProviderWithoutNoArgConstructor() {
    val result = compile(
      """
      @Mugshot
      @Preview
      @Composable
      fun SamplePreview(
        @PreviewParameter(SampleProvider::class) text: String
      ) = Unit

      class SampleProvider(private val seed: String) : PreviewParameterProvider<String> {
        override val values: Sequence<String> = sequenceOf(seed)
      }
      """
    )

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    assertThat(result.messages)
      .contains("e: [ksp] test.SampleProvider needs a no-argument constructor to generate a snapshot.")
  }

  /** Wraps [body] in the imports every case needs and compiles it with the processor. */
  private fun compile(body: String) = compilation(body).compile()

  private fun compilation(body: String): KotlinCompilation {
    val source = SourceFile.kotlin(
      "SamplePreview.kt",
      """
      package test

      import androidx.compose.runtime.Composable
      import androidx.compose.ui.tooling.preview.Preview
      import androidx.compose.ui.tooling.preview.PreviewParameter
      import androidx.compose.ui.tooling.preview.PreviewParameterProvider
      import uk.co.fractalmotion.mugshot.annotations.Mugshot
      import uk.co.fractalmotion.mugshot.annotations.MugshotDevice
      import uk.co.fractalmotion.mugshot.annotations.MugshotDevices
      import uk.co.fractalmotion.mugshot.annotations.MugshotFontScales
      import uk.co.fractalmotion.mugshot.annotations.MugshotFullScreen
      import uk.co.fractalmotion.mugshot.annotations.MugshotLightDark
      import uk.co.fractalmotion.mugshot.annotations.MugshotLocales
      import uk.co.fractalmotion.mugshot.annotations.MugshotMatrix
      import uk.co.fractalmotion.mugshot.annotations.MugshotShrink
      import uk.co.fractalmotion.mugshot.annotations.MugshotWear

      ${body.trimIndent()}
      """.trimIndent()
    )

    return KotlinCompilation()
      .apply {
        workingDir = File(temporaryFolder.root, "debug")
        inheritClassPath = true
        sources = listOf(source) + COMPOSE_SOURCES + RUNTIME_SOURCE
        verbose = false
        // Needed for the PreviewParameterProvider stub, which uses @JvmDefaultWithCompatibility.
        kotlincArguments = listOf("-Xjvm-default=all")

        configureKsp {
          allWarningsAsErrors = true
          kspProcessorOptions += "uk.co.fractalmotion.mugshot.preview.namespace" to TEST_NAMESPACE
          kspIncremental = true
          symbolProcessorProviders += PreviewProcessorProvider()
        }
      }
  }

  private companion object {
    private const val TEST_NAMESPACE = "test"

    private val COMPOSE_SOURCES = listOf(
      SourceFile.kotlin(
        "Composable.kt",
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
        """.trimIndent()
      ),
      SourceFile.kotlin(
        "PreviewAnnotation.kt",
        """
        package androidx.compose.ui.tooling.preview

        @Retention(AnnotationRetention.BINARY)
        @Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
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
          val wallpaper: Int = 0
        )
        """.trimIndent()
      ),
      SourceFile.kotlin(
        "PreviewParameter.kt",
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
        """.trimIndent()
      )
    )

    /**
     * Stubs of the runtime types.
     *
     * Stubbed rather than depended on because the real module compiles against Compose, and these
     * tests substitute their own minimal `@Composable`.
     */
    private val RUNTIME_SOURCE = SourceFile.kotlin(
      "MugshotPreviewCase.kt",
      """
      package uk.co.fractalmotion.mugshot.preview.runtime

      import androidx.compose.runtime.Composable

      enum class MugshotPreviewDevice {
        DEFAULT, PHONE, FOLDABLE, TABLET, LANDSCAPE, WEAR_ROUND, WEAR_SQUARE
      }

      enum class MugshotPreviewRenderingMode { NORMAL, SHRINK, V_SCROLL }

      data class MugshotPreviewConfig(
        val device: MugshotPreviewDevice = MugshotPreviewDevice.DEFAULT,
        val nightMode: Boolean = false,
        val fontScale: Float = 1f,
        val locale: String? = null,
        val rtl: Boolean = false,
        val renderingMode: MugshotPreviewRenderingMode = MugshotPreviewRenderingMode.NORMAL
      )

      class MugshotPreviewCase(
        val snapshotName: String,
        val config: MugshotPreviewConfig,
        private val frames: () -> List<@Composable () -> Unit>
      ) {
        fun frames(): List<@Composable () -> Unit> = frames.invoke()
        override fun toString(): String = snapshotName
      }

      fun <T> parameterizedFrames(
        values: Sequence<T>,
        limit: Int,
        composable: @Composable (T) -> Unit
      ): List<@Composable () -> Unit> =
        values.take(limit).map<T, @Composable () -> Unit> { value -> { composable(value) } }.toList()
      """.trimIndent()
    )
  }
}
