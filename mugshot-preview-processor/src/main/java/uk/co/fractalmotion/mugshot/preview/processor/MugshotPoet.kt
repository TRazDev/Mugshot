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

internal class MugshotPoet(
  private val logger: KSPLogger,
  private val namespace: String
) {
  fun buildFiles(functions: Sequence<KSFunctionDeclaration>, isTest: Boolean) =
    if (isTest) {
      emptyList()
    } else {
      if (functions.count() == 0) {
        logger.info("No functions found with @Mugshot annotation.")
        emptyList()
      } else {
        listOf(
          buildAnnotationsFile(
            fileName = "MugshotPreviews",
            propertyName = "mugshotPreviews",
            functions = functions
          )
        )
      }
    }

  @Suppress("SameParameterValue")
  private fun buildAnnotationsFile(fileName: String, propertyName: String, functions: Sequence<KSFunctionDeclaration>) =
    FileSpec.scriptBuilder(fileName, namespace)
      .addCode(
        buildCodeBlock {
          addStatement(
            "internal val %L: List<%L.MugshotPreviewData> = buildList {",
            propertyName,
            PREVIEW_RUNTIME_PACKAGE_NAME
          )
          indent()

          functions.process { func, previewParam ->
            val snapshotName = func.snapshotName(namespace)
            val qualifiedName = func.qualifiedName?.asString()
            when {
              func.getVisibility() == Visibility.PRIVATE -> {
                logger.error("$qualifiedName is private. Make it internal or public to generate a snapshot.")
              }

              previewParam != null -> addParameterized(
                function = func,
                snapshotName = snapshotName,
                previewParameter = previewParam
              )

              func.parameters.isNotEmpty() -> {
                logger.error(
                  "$qualifiedName has parameters. Only @PreviewParameter parameters are supported."
                )
              }

              else -> addDefault(
                function = func,
                snapshotName = snapshotName
              )
            }
          }

          unindent()
          add("}")
        }
      )
      .build()

  /**
   * Emits one entry per annotated function.
   *
   * A function may carry several `@Preview` annotations, but none of their arguments are read, so
   * every one of them would render the same snapshot under the same name. Collapsing to a single
   * entry per function avoids generating colliding snapshot names.
   */
  private fun Sequence<KSFunctionDeclaration>.process(block: (KSFunctionDeclaration, KSValueParameter?) -> Unit) =
    filter { func -> func.annotations.findPreviews().any() }
      .forEach { func ->
        val previewParam = func.parameters.firstOrNull { param ->
          param.annotations.any { it.isPreviewParameter() }
        }
        block(func, previewParam)
      }

  private fun CodeBlock.Builder.addDefault(function: KSFunctionDeclaration, snapshotName: String) {
    addStatement("add(")
    indent()
    addStatement("%L.MugshotPreviewData(", PREVIEW_RUNTIME_PACKAGE_NAME)
    indent()
    addStatement("snapshotName = %S,", snapshotName)
    addStatement("composable = { %L() }", function.qualifiedName?.asString())
    unindent()
    addStatement(")")
    unindent()
    addStatement(")")
  }

  /**
   * Emits an entry per value supplied by the `@PreviewParameter` provider.
   *
   * The values cannot be enumerated here — they are arbitrary Kotlin evaluated at test runtime — so
   * this delegates to `parameterizedPreviews`, which expands them and indexes the snapshot names.
   */
  private fun CodeBlock.Builder.addParameterized(
    function: KSFunctionDeclaration,
    snapshotName: String,
    previewParameter: KSValueParameter
  ) {
    val qualifiedName = function.qualifiedName?.asString()

    if (function.parameters.size > 1) {
      logger.error("$qualifiedName has parameters beyond its @PreviewParameter. These aren't supported.")
      return
    }

    val annotation = previewParameter.annotations.first { it.isPreviewParameter() }
    val provider = annotation.argumentOf("provider") as? KSType
    val providerDeclaration = provider?.declaration as? KSClassDeclaration
    if (providerDeclaration == null) {
      logger.error("Could not resolve the @PreviewParameter provider of $qualifiedName.")
      return
    }

    val providerName = providerDeclaration.qualifiedName?.asString()
    if (providerDeclaration.getVisibility() == Visibility.PRIVATE) {
      logger.error("$providerName is private. Make it internal or public to generate a snapshot.")
      return
    }

    val isObject = providerDeclaration.classKind == ClassKind.OBJECT
    if (!isObject && !providerDeclaration.hasNoArgConstructor()) {
      logger.error("$providerName needs a no-argument constructor to generate a snapshot.")
      return
    }

    val limit = annotation.argumentOf("limit") as? Int ?: Int.MAX_VALUE

    addStatement("addAll(")
    indent()
    addStatement("%L.parameterizedPreviews(", PREVIEW_RUNTIME_PACKAGE_NAME)
    indent()
    addStatement("snapshotName = %S,", snapshotName)
    addStatement("values = %L.values,", if (isObject) providerName else "$providerName()")
    addStatement("limit = %L", limit)
    unindent()
    addStatement(") { %L(it) }", qualifiedName)
    unindent()
    addStatement(")")
  }

  private fun KSClassDeclaration.hasNoArgConstructor() =
    getConstructors().any { constructor -> constructor.parameters.all { it.hasDefault } }

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

private const val PREVIEW_RUNTIME_PACKAGE_NAME = "uk.co.fractalmotion.mugshot.preview.runtime"

internal fun KSAnnotation.isPreview() = qualifiedName() == "androidx.compose.ui.tooling.preview.Preview"
internal fun KSAnnotation.isPreviewParameter() =
  qualifiedName() == "androidx.compose.ui.tooling.preview.PreviewParameter"

internal fun KSAnnotation.qualifiedName() = declaration().qualifiedName?.asString() ?: ""
internal fun KSAnnotation.declaration() = annotationType.resolve().declaration

internal fun KSAnnotation.argumentOf(name: String) = arguments.firstOrNull { it.name?.asString() == name }?.value

/**
 * when the same annotations are applied higher in the tree, an endless recursive lookup can occur.
 * using a stack to keep to a record of each symbol lets us break when we hit one we've already encountered
 */
internal fun Sequence<KSAnnotation>.findPreviews(stack: Set<KSAnnotation> = setOf()): Sequence<KSAnnotation> {
  val direct = filter { it.isPreview() }
  val indirect = filterNot { it.isPreview() || stack.contains(it) }
    .map { it.declaration().annotations.findPreviews(stack.plus(it)) }
    .flatten()
  return direct.plus(indirect)
}
