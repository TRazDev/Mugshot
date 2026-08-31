package uk.co.fractalmotion.mugshot.preview.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.File

public class PreviewProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): PreviewProcessor = PreviewProcessor(environment)
}

public class PreviewProcessor(
  private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
  private val logger = environment.logger
  private var invoked = false

  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (invoked) {
      // Only need a single round
      return emptyList()
    }
    invoked = true

    val allFiles = resolver.getAllFiles().toList()
    val namespace = environment.options[NAMESPACE_OPTION]
    if (namespace == null) {
      logger.error("Missing KSP option '$NAMESPACE_OPTION'. Apply the Mugshot Gradle plugin to set it.")
      return emptyList()
    }

    val dependencies = Dependencies(true, *allFiles.toTypedArray())
    if (isTestSourceSet(namespace, dependencies)) {
      // Nothing to do: the catalogue belongs to the main compilation, and generating a second one
      // here would shadow it with an empty list.
      return emptyList()
    }

    val functions = resolver.mugshotFunctions()
    logger.log("found ${functions.size} function(s)")

    // Generated unconditionally, even with nothing to snapshot: the Gradle plugin always writes a
    // test that reads mugshotPreviewCases, and that test has to compile in a module which has not
    // annotated anything yet.
    MugshotPoet(logger, namespace).buildFiles(functions.asSequence()).forEach { file ->
      logger.log("writing file: ${file.packageName}.${file.name}.kt")
      file.writeTo(environment.codeGenerator, dependencies)
    }

    return functions.filterNot { it.validate() }
  }

  /**
   * Whether this run is processing a test compilation rather than a main one.
   *
   * KSP's per-variant test configuration inherits the main one — `kspDebugUnitTest`'s processor
   * classpath contains everything on `kspDebug` — so the Gradle plugin cannot keep the processor
   * off the test compilation by scoping configurations. The source set has to be recognised here.
   *
   * KSP exposes no API for it, so this writes a throwaway file and reads the source set out of the
   * path it is given: `.../build/generated/ksp/<sourceSet>/resources/...`. Parsed by path segment
   * rather than by matching the whole string, so an unexpected directory layout degrades to "not a
   * test" instead of matching something arbitrary.
   */
  private fun isTestSourceSet(namespace: String, dependencies: Dependencies): Boolean {
    environment.codeGenerator.createNewFile(dependencies, namespace, "mugshotSourceSet", "txt")
    val generated = environment.codeGenerator.generatedFile.firstOrNull() ?: return false
    val segments = generated.absolutePath.split(File.separatorChar)
    val sourceSet = segments.getOrNull(segments.lastIndexOf("ksp") + 1).orEmpty()
    generated.writeText(sourceSet)
    return sourceSet.endsWith("UnitTest") || sourceSet.endsWith("AndroidTest")
  }

  /**
   * Every function reachable from `@Mugshot`, directly or through a bundle annotation.
   *
   * `getSymbolsWithAnnotation` reports only direct usages, so a function wearing a team's own
   * `@OurScreenshots` — itself annotated `@Mugshot` — is invisible to a single lookup. Because
   * `@Mugshot` targets annotation classes as well as functions, that first lookup returns the
   * bundle declarations too, and each becomes another name to search for.
   *
   * Sorted by qualified name so the generated case order, and therefore every golden name, is
   * stable across builds regardless of the order KSP hands symbols back.
   */
  private fun Resolver.mugshotFunctions(): List<KSFunctionDeclaration> {
    val functions = linkedSetOf<KSFunctionDeclaration>()
    val searched = mutableSetOf<String>()
    val pending = ArrayDeque(listOf(MUGSHOT_ANNOTATION))

    while (pending.isNotEmpty()) {
      val annotationName = pending.removeFirst()
      if (!searched.add(annotationName)) continue

      getSymbolsWithAnnotation(annotationName).forEach { symbol ->
        when {
          symbol is KSFunctionDeclaration -> functions += symbol
          symbol is KSClassDeclaration && symbol.classKind == ClassKind.ANNOTATION_CLASS ->
            symbol.qualifiedName?.asString()?.let { pending += it }
          else -> Unit
        }
      }
    }

    return functions.sortedBy { it.qualifiedName?.asString().orEmpty() }
  }

  private fun KSPLogger.log(message: String) = info("PreviewProcessor - $message")

  private companion object {
    private const val NAMESPACE_OPTION = "uk.co.fractalmotion.mugshot.preview.namespace"
  }
}
