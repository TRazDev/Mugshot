package uk.co.fractalmotion.mugshot.preview.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType

internal const val ANNOTATIONS_PACKAGE: String = "uk.co.fractalmotion.mugshot.annotations"
internal const val MUGSHOT_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.Mugshot"
internal const val PREVIEW_ANNOTATION: String = "androidx.compose.ui.tooling.preview.Preview"
internal const val PREVIEW_PARAMETER_ANNOTATION: String =
  "androidx.compose.ui.tooling.preview.PreviewParameter"

internal const val SHRINK_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotShrink"
internal const val FULL_SCREEN_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotFullScreen"
internal const val DEVICES_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotDevices"
internal const val WEAR_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotWear"
internal const val LIGHT_DARK_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotLightDark"
internal const val FONT_SCALES_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotFontScales"
internal const val LOCALES_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotLocales"
internal const val MATRIX_ANNOTATION: String = "$ANNOTATIONS_PACKAGE.MugshotMatrix"

internal val AXIS_ANNOTATIONS: Set<String> = setOf(
  SHRINK_ANNOTATION,
  FULL_SCREEN_ANNOTATION,
  DEVICES_ANNOTATION,
  WEAR_ANNOTATION,
  LIGHT_DARK_ANNOTATION,
  FONT_SCALES_ANNOTATION,
  LOCALES_ANNOTATION,
  MATRIX_ANNOTATION
)

internal val DEFAULT_DEVICES: List<String> = listOf("PHONE", "FOLDABLE", "TABLET", "LANDSCAPE")
internal val DEFAULT_FONT_SCALES: List<Float> = listOf(1f, 1.5f, 2f)

/**
 * Language subtags written right to left.
 *
 * Resolved here rather than at test runtime so the generated config is fully explicit. `ar-rXB`,
 * the bidi pseudolocale, matches on its `ar` subtag; `en-rXA`, the accent pseudolocale, does not.
 */
private val RTL_LANGUAGES = setOf("ar", "dv", "fa", "he", "iw", "ps", "sd", "ug", "ur", "yi")

internal fun isRtlLocale(tag: String): Boolean = languageSubtagOf(tag) in RTL_LANGUAGES

/**
 * The language subtag of an Android locale qualifier.
 *
 * Handles both qualifier forms. The common one separates subtags with `-`, as in `ar` or
 * `ar-rXB`. The BCP 47 form Android writes as `b+ar+u+nu+arab` separates them with `+`, and is
 * the only way to pin a numbering system -- without which digit shapes come from the host JDK's
 * CLDR data and change between JDK releases. Splitting on `-` alone left that form unrecognised,
 * so an Arabic preview requested through it rendered left-to-right.
 */
private fun languageSubtagOf(tag: String): String {
  val withoutBcp47Prefix = tag.removePrefix("b+").removePrefix("B+")
  return withoutBcp47Prefix.substringBefore('-').substringBefore('+').lowercase()
}

/** One rendering configuration, ready to be emitted as a `MugshotPreviewConfig`. */
internal data class AxisCombination(
  val device: String,
  val nightMode: Boolean,
  val fontScale: Float,
  val locale: String?,
  val renderingMode: String
)

/**
 * The axes resolved for one annotated function, before they are multiplied out.
 *
 * Every axis carries its baseline value in its own value list — `devices` includes `PHONE`,
 * `nightModes` includes light, `fontScales` includes `1f` — so a bare `@Mugshot` yields exactly one
 * combination and each added annotation multiplies rather than replaces.
 */
internal data class ResolvedAxes(
  val devices: List<String> = listOf("DEFAULT"),
  val nightModes: List<Boolean> = listOf(false),
  val fontScales: List<Float> = listOf(1f),
  val locales: List<String?> = listOf(null),
  val renderingMode: String = "NORMAL"
) {
  /** The cross-product, in a fixed axis order so golden names never churn between builds. */
  fun combinations(): List<AxisCombination> =
    devices.flatMap { device ->
      nightModes.flatMap { night ->
        fontScales.flatMap { scale ->
          locales.map { locale ->
            AxisCombination(device, night, scale, locale, renderingMode)
          }
        }
      }
    }

  /**
   * The suffix distinguishing one combination's golden from its siblings.
   *
   * Only axes that actually vary contribute, so adding an annotation never renames the goldens of
   * previews that did not opt into it.
   */
  fun suffix(combination: AxisCombination): String {
    val parts = buildList {
      if (devices.size > 1 || combination.device != "DEFAULT") {
        add(combination.device.toPascalCase())
      }
      if (nightModes.size > 1) add(if (combination.nightMode) "Dark" else "Light")
      if (fontScales.size > 1) add("Font${(combination.fontScale * 100).toInt()}")
      if (locales.size > 1) add(combination.locale ?: "Default")
    }
    return if (parts.isEmpty()) "" else parts.joinToString(separator = "_", prefix = "_")
  }
}

private fun String.toPascalCase(): String =
  split('_').joinToString("") { part -> part.lowercase().replaceFirstChar { it.uppercaseChar() } }

/**
 * Collects annotations matching [qualifiedNames], following meta-annotations.
 *
 * A team can bundle several axes behind one name, so an axis may sit one or more levels above the
 * function. The visited [stack] breaks the cycle a self-referencing annotation would otherwise
 * create.
 */
internal fun Sequence<KSAnnotation>.findAnnotations(
  qualifiedNames: Set<String>,
  stack: Set<KSAnnotation> = setOf()
): Sequence<KSAnnotation> {
  val direct = filter { it.qualifiedName() in qualifiedNames }
  val indirect = filterNot { it.qualifiedName() in qualifiedNames || stack.contains(it) }
    .map { it.declaration().annotations.findAnnotations(qualifiedNames, stack.plus(it)) }
    .flatten()
  return direct.plus(indirect)
}

internal fun Sequence<KSAnnotation>.findPreviews(): Sequence<KSAnnotation> = findAnnotations(setOf(PREVIEW_ANNOTATION))

internal fun KSAnnotation.isPreviewParameter(): Boolean = qualifiedName() == PREVIEW_PARAMETER_ANNOTATION

internal fun KSAnnotation.qualifiedName(): String = declaration().qualifiedName?.asString() ?: ""

internal fun KSAnnotation.declaration(): KSDeclaration = annotationType.resolve().declaration

internal fun KSAnnotation.argumentOf(name: String): Any? = arguments.firstOrNull { it.name?.asString() == name }?.value

/**
 * Reads an enum entry from an annotation argument.
 *
 * KSP models an enum constant as a `KSType` whose declaration is the entry itself, but the exact
 * shape has moved between versions, so this falls back to the value's own rendering.
 */
internal fun Any?.enumEntryName(): String? =
  when (this) {
    null -> null
    is KSType -> declaration.simpleName.asString()
    is KSDeclaration -> simpleName.asString()
    else -> toString().substringAfterLast('.').takeIf { it.isNotBlank() }
  }
