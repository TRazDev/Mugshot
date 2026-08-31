package uk.co.fractalmotion.mugshot.preview.processor

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.buildCodeBlock

private const val RUNTIME = "uk.co.fractalmotion.mugshot.preview.runtime"
private const val COMPOSABLE = "androidx.compose.runtime.Composable"

internal class MugshotPoet(
  private val logger: KSPLogger,
  private val namespace: String
) {
  fun buildFiles(functions: Sequence<KSFunctionDeclaration>): List<FileSpec> =
    listOf(
      FileSpec.scriptBuilder("MugshotPreviews", namespace)
        .addCode(buildCases(functions))
        .build()
    )

  private fun buildCases(functions: Sequence<KSFunctionDeclaration>) =
    buildCodeBlock {
      addStatement("internal val mugshotPreviewCases: List<%L.MugshotPreviewCase> = buildList {", RUNTIME)
      indent()
      functions.forEach { function -> addFunction(function) }
      unindent()
      add("}")
    }

  private fun CodeBlock.Builder.addFunction(function: KSFunctionDeclaration) {
    val qualifiedName = function.qualifiedName?.asString() ?: return

    // A @Mugshot function with no reachable @Preview is skipped rather than failed; the lint check
    // PreviewAnnotationNotFound reports it, with a location the compiler cannot give us here.
    if (!function.annotations.findPreviews().any()) return

    if (function.getVisibility() == Visibility.PRIVATE) {
      logger.error("$qualifiedName is private. Make it internal or public to generate a snapshot.")
      return
    }

    val previewParameter = function.parameters.firstOrNull { parameter ->
      parameter.annotations.any { it.isPreviewParameter() }
    }
    if (previewParameter == null && function.parameters.isNotEmpty()) {
      logger.error("$qualifiedName has parameters. Only @PreviewParameter parameters are supported.")
      return
    }
    if (previewParameter != null && function.parameters.size > 1) {
      logger.error("$qualifiedName has parameters beyond its @PreviewParameter. These aren't supported.")
      return
    }

    val frames = frames(function, previewParameter) ?: return
    val axes = function.resolveAxes()
    val baseName = function.snapshotName(namespace)

    axes.combinations().forEach { combination ->
      addCase(
        snapshotName = baseName + axes.suffix(combination),
        combination = combination,
        frames = frames
      )
    }
  }

  /** The literal emitted for the case's `frames` lambda, or null when it could not be resolved. */
  private fun frames(function: KSFunctionDeclaration, previewParameter: KSValueParameter?): String? {
    val qualifiedName = function.qualifiedName?.asString() ?: return null
    if (previewParameter == null) {
      return "{ listOf<@$COMPOSABLE () -> Unit>({ $qualifiedName() }) }"
    }

    val annotation = previewParameter.annotations.first { it.isPreviewParameter() }
    val provider = annotation.argumentOf("provider") as? KSType
    val declaration = provider?.declaration as? KSClassDeclaration
    if (declaration == null) {
      logger.error("Could not resolve the @PreviewParameter provider of $qualifiedName.")
      return null
    }

    val providerName = declaration.qualifiedName?.asString()
    if (declaration.getVisibility() == Visibility.PRIVATE) {
      logger.error("$providerName is private. Make it internal or public to generate a snapshot.")
      return null
    }

    val isObject = declaration.classKind == ClassKind.OBJECT
    val hasNoArgConstructor =
      declaration.getConstructors().any { constructor -> constructor.parameters.all { it.hasDefault } }
    if (!isObject && !hasNoArgConstructor) {
      logger.error("$providerName needs a no-argument constructor to generate a snapshot.")
      return null
    }

    val limit = annotation.argumentOf("limit") as? Int ?: Int.MAX_VALUE
    val instance = if (isObject) providerName else "$providerName()"
    return "{ $RUNTIME.parameterizedFrames($instance.values, $limit) { $qualifiedName(it) } }"
  }

  private fun CodeBlock.Builder.addCase(snapshotName: String, combination: AxisCombination, frames: String) {
    addStatement("add(")
    indent()
    addStatement("%L.MugshotPreviewCase(", RUNTIME)
    indent()
    addStatement("snapshotName = %S,", snapshotName)
    addStatement("config = %L.MugshotPreviewConfig(", RUNTIME)
    indent()
    addStatement("device = %L.MugshotPreviewDevice.%L,", RUNTIME, combination.device)
    addStatement("nightMode = %L,", combination.nightMode)
    addStatement("fontScale = %Lf,", combination.fontScale)
    if (combination.locale == null) {
      addStatement("locale = null,")
    } else {
      addStatement("locale = %S,", combination.locale)
    }
    addStatement("rtl = %L,", combination.locale?.let { isRtlLocale(it) } ?: false)
    addStatement("renderingMode = %L.MugshotPreviewRenderingMode.%L", RUNTIME, combination.renderingMode)
    unindent()
    addStatement("),")
    addStatement("frames = %L", frames)
    unindent()
    addStatement(")")
    unindent()
    addStatement(")")
  }

  private fun KSFunctionDeclaration.resolveAxes(): ResolvedAxes {
    val axes = annotations.findAnnotations(AXIS_ANNOTATIONS).toList()
    fun axis(name: String): KSAnnotation? = axes.firstOrNull { it.qualifiedName() == name }

    val matrix = axis(MATRIX_ANNOTATION) != null
    val devicesAnnotation = axis(DEVICES_ANNOTATION)
    val wear = axis(WEAR_ANNOTATION) != null
    val fontScalesAnnotation = axis(FONT_SCALES_ANNOTATION)
    val localesAnnotation = axis(LOCALES_ANNOTATION)

    val devices = buildList {
      when {
        devicesAnnotation != null -> addAll(devicesAnnotation.deviceNames())
        matrix -> addAll(DEFAULT_DEVICES)
      }
      if (wear) addAll(listOf("WEAR_ROUND", "WEAR_SQUARE"))
    }.ifEmpty { listOf("DEFAULT") }

    val nightModes =
      if (matrix || axis(LIGHT_DARK_ANNOTATION) != null) listOf(false, true) else listOf(false)

    val fontScales = when {
      fontScalesAnnotation != null -> fontScalesAnnotation.floatList("scales").ifEmpty { DEFAULT_FONT_SCALES }
      matrix -> DEFAULT_FONT_SCALES
      else -> listOf(1f)
    }

    // The locale axis keeps a null baseline so that naming a locale adds coverage rather than
    // replacing the default-locale golden, matching how every other axis includes its own baseline.
    val locales = buildList<String?> {
      add(null)
      localesAnnotation?.stringList("locales")?.let { addAll(it) }
    }.distinct()

    val renderingMode = when {
      axis(FULL_SCREEN_ANNOTATION) != null -> "V_SCROLL"
      axis(SHRINK_ANNOTATION) != null -> "SHRINK"
      else -> "NORMAL"
    }

    return ResolvedAxes(devices, nightModes, fontScales, locales, renderingMode)
  }

  private fun KSAnnotation.deviceNames(): List<String> {
    val declared = (argumentOf("devices") as? List<*>)?.mapNotNull { it.enumEntryName() }.orEmpty()
    return declared.ifEmpty { DEFAULT_DEVICES }
  }

  private fun KSAnnotation.floatList(name: String): List<Float> =
    (argumentOf(name) as? List<*>)?.mapNotNull { (it as? Number)?.toFloat() }.orEmpty()

  private fun KSAnnotation.stringList(name: String): List<String> =
    (argumentOf(name) as? List<*>)?.mapNotNull { it as? String }.orEmpty()

  private fun KSFunctionDeclaration.snapshotName(namespace: String) =
    buildList {
      with(containingFile!!) {
        add(
          "${packageName.asString()}.${fileName.removeSuffix(".kt")}"
            .removePrefix("$namespace.")
            .replace(".", "_")
        )
      }
      add(simpleName.asString())
    }.joinToString("_")
}
