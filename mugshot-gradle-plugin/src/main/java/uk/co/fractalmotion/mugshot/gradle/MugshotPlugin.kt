/*
 * Copyright (C) 2019 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.fractalmotion.mugshot.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.DynamicFeatureAndroidComponentsExtension
import com.android.build.api.variant.HasUnitTest
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.UnitTest
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.artifacts.transform.UnzipTransform
import org.gradle.api.internal.tasks.testing.report.TestReporter
import org.gradle.api.logging.LogLevel.LIFECYCLE
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.operations.BuildOperationRunner
import org.gradle.internal.os.OperatingSystem
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import uk.co.fractalmotion.mugshot.gradle.instrumentation.ResourcesCompatVisitorFactory
import uk.co.fractalmotion.mugshot.gradle.reporting.DiffImage
import uk.co.fractalmotion.mugshot.gradle.reporting.MugshotTestReporter
import uk.co.fractalmotion.mugshot.gradle.utils.artifactViewFor
import uk.co.fractalmotion.mugshot.gradle.utils.capitalize
import uk.co.fractalmotion.mugshot.gradle.utils.relativize
import java.util.Locale
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Suppress("unused")
public class MugshotPlugin @Inject constructor(
  private val providerFactory: ProviderFactory,
  private val buildOperationRunner: BuildOperationRunner,
  private val buildOperationExecutor: BuildOperationExecutor
) : Plugin<Project> {
  override fun apply(project: Project) {
    val supportedPlugins = listOf(
      "com.android.application",
      "com.android.library",
      "com.android.dynamic-feature",
      ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY_PLUGIN
    )
    project.afterEvaluate {
      check(supportedPlugins.any { project.plugins.hasPlugin(it) }) {
        "One of ${supportedPlugins.joinToString(", ")} must be applied for Mugshot to work properly."
      }
    }

    supportedPlugins.forEach { plugin ->
      project.plugins.withId(plugin) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        when (androidComponents) {
          is LibraryAndroidComponentsExtension,
          is ApplicationAndroidComponentsExtension,
          is DynamicFeatureAndroidComponentsExtension,
          is KotlinMultiplatformAndroidComponentsExtension -> Unit
          // exhaustive to avoid potential breaking changes in future AGP releases
          else -> error("${androidComponents.javaClass.name} from $plugin is not supported in Mugshot")
        }
        project.setupMugshot(androidComponents)
      }
    }
  }

  private fun Project.setupMugshot(extension: AndroidComponentsExtension<*, *, *>) {
    val isMultiplatformProject: Boolean = extension is KotlinMultiplatformAndroidComponentsExtension ||
      project.plugins.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN)
    addTestDependency()
    configurePreviewProcessor(extension, isMultiplatformProject)

    val layoutlibNativeRuntimeFileCollection = project.setupLayoutlibRuntimeDependency()
    val layoutlibResourcesFileCollection = project.setupLayoutlibResourcesDependency()

    // Create anchor tasks for all variants.
    val verifyVariants = project.tasks.register("verifyMugshot") {
      it.group = VERIFICATION_GROUP
      it.description = "Run screenshot tests for all variants"
    }
    val recordVariants = project.tasks.register("recordMugshot") {
      it.group = VERIFICATION_GROUP
      it.description = "Record golden images for all variants"
    }
    val cleanRecordVariants = project.tasks.register("cleanRecordMugshot") {
      it.group = VERIFICATION_GROUP
      it.description = "Clean and record golden images for all variants"
    }
    val deleteSnapshots = project.tasks.register("deleteMugshotSnapshots") {
      it.group = VERIFICATION_GROUP
      it.description = "Delete all golden images"
    }

    extension.onVariants { variant ->
      val variantSlug = variant.name.capitalize()
      val testVariant = (variant as? HasUnitTest)?.unitTest ?: return@onVariants
      val snapshotOutputDir = snapshotDir(testVariant)

      val deleteVariantSnapshot =
        project.tasks.register("delete${variantSlug}MugshotSnapshots", Delete::class.java) {
          it.group = VERIFICATION_GROUP
          it.description = "Delete all golden images for variant '$variantSlug'"
          val files = project.fileTree(snapshotOutputDir) { tree ->
            tree.include("**/*.webp")
            // Snapshots recorded before the switch to WebP.
            tree.include("**/*.png")
          }
          it.delete(files)
        }
      deleteSnapshots.configure { it.dependsOn(deleteVariantSnapshot) }

      val projectDirectory = project.layout.projectDirectory
      val buildDirectory = project.layout.buildDirectory
      val gradleUserHomeDir = project.gradle.gradleUserHomeDir
      val reportOutputDir =
        project.extensions.getByType(ReportingExtension::class.java).baseDirectory.dir("mugshot/${variant.name}")

      // AGP < 9 does not fully initialize ASM instrumentation for Android KMP variants, causing
      // `lateinit property visitorFactory has not been initialized` during configuration.
      // This transform is a best-effort fix for ResourcesCompat font loading, so skip it for KMP
      // projects until AGP 9+.
      val testInstrumentation = testVariant.instrumentation
      testInstrumentation.transformClassesWith(
        ResourcesCompatVisitorFactory::class.java,
        InstrumentationScope.ALL
      ) { }
      testInstrumentation.setAsmFramesComputationMode(
        FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
      )

      val sources = AndroidVariantSources(variant)

      val writeResourcesTask = project.tasks.register(
        "prepareMugshot${variantSlug}Resources",
        PrepareResourcesTask::class.java
      ) { task ->
        val nonTransitiveRClassEnabled =
          project.providers.gradleProperty("android.nonTransitiveRClass").orNull?.toBoolean() ?: true
        val gradleHomeDir = projectDirectory.dir(project.gradle.gradleUserHomeDir.path)

        task.packageName.set(variant.namespace)
        task.artifactFiles.from(sources.packageAwareArtifactFiles)
        task.nonTransitiveRClassEnabled.set(nonTransitiveRClassEnabled)
        task.targetSdkVersion.set(targetSdk())
        task.projectResourceDirs.set(sources.localResourceDirs.relativize(projectDirectory))
        task.moduleResourceDirs.set(sources.moduleResourceDirs.relativize(projectDirectory))
        task.aarExplodedDirs.set(sources.aarExplodedDirs.relativize(gradleHomeDir))
        task.projectAssetDirs.set(
          sources.localAssetDirs.relativize(projectDirectory)
            .zip(sources.moduleAssetDirs.relativize(projectDirectory), List<String>::plus)
        )
        task.aarAssetDirs.set(sources.aarAssetDirs.relativize(gradleHomeDir))
        task.mugshotResources.set(buildDirectory.file("intermediates/mugshot/${variant.name}/resources.json"))
      }

      val testVariantSlug = testVariant.name.capitalize()

      val testTasks = project.tasks.named { it == "test$testVariantSlug" }
      testTasks.configureEach { it.dependsOn(writeResourcesTask) }

      val recordTaskProvider = project.tasks.register("recordMugshot$variantSlug", MugshotTask::class.java) {
        it.group = VERIFICATION_GROUP
        it.description = "Record golden images for variant '${variant.name}'"
        it.mustRunAfter(deleteSnapshots)
      }
      recordVariants.configure { it.dependsOn(recordTaskProvider) }
      val cleanRecordTaskProvider = project.tasks.register("cleanRecordMugshot$variantSlug") {
        it.group = VERIFICATION_GROUP
        it.description = "Clean and record golden images for variant '${variant.name}'"
        it.dependsOn(deleteSnapshots, recordTaskProvider)
      }
      cleanRecordVariants.configure { it.dependsOn(cleanRecordTaskProvider) }
      val verifyTaskProvider = project.tasks.register("verifyMugshot$variantSlug", MugshotTask::class.java) {
        it.group = VERIFICATION_GROUP
        it.description = "Run screenshot tests for variant '${variant.name}'"
      }
      verifyVariants.configure { it.dependsOn(verifyTaskProvider) }

      val isRecordRun = project.objects.property(Boolean::class.java)
      val isVerifyRun = project.objects.property(Boolean::class.java)

      project.gradle.taskGraph.whenReady { graph ->
        isRecordRun.set(recordTaskProvider.map { graph.hasTask(it) })
        isVerifyRun.set(verifyTaskProvider.map { graph.hasTask(it) })
      }

      val overwriteOnMaxPercentDifferenceProvider = project.overwriteOnMaxPercentDifferenceProvider()
      val mugshotGradlePropertiesProvider =
        project.providers.gradlePropertiesPrefixedBy("uk.co.fractalmotion.mugshot")
      val failureDir = buildDirectory.dir("mugshot/failures/${variant.name}")
      val testTaskProvider = testTasks.withType(Test::class.java)
      testTaskProvider.configureEach { test ->
        val localResourceDirs = sources.localResourceDirs ?: providerFactory.provider { emptyList() }
        val localAssetDirs = sources.localAssetDirs ?: providerFactory.provider { emptyList() }

        test.setTestReporter(
          MugshotTestReporter(
            buildOperationRunner = buildOperationRunner,
            buildOperationExecutor = buildOperationExecutor,
            diffRegistryFactory = createDiffRegistryFactory(failureDir, isVerifyRun)
          )
        )

        // Absolute paths passed via `systemProperties` (an @Input) would pollute the build-cache
        // key and break relocatability. Supply them as @Internal JVM args instead (#1874); task
        // content is tracked separately via the path-sensitive file inputs below.
        val pathSystemProperties = project.objects.mapProperty(String::class.java, String::class.java)
        pathSystemProperties.put(
          "mugshot.test.resources",
          writeResourcesTask.flatMap { it.mugshotResources.asFile }.map { it.path }
        )
        pathSystemProperties.put("mugshot.project.dir", projectDirectory.toString())
        pathSystemProperties.put("mugshot.build.dir", buildDirectory.map { it.toString() })
        pathSystemProperties.put("mugshot.report.dir", reportOutputDir.map { it.toString() })
        pathSystemProperties.put("mugshot.artifacts.cache.dir", gradleUserHomeDir.path)
        test.jvmArgumentProviders.add(MugshotSystemPropertiesArgumentProvider(pathSystemProperties))

        test.inputs.property("mugshot.test.record", isRecordRun)
        test.inputs.property("mugshot.test.verify", isVerifyRun)
        test.inputs.property("mugshot.gradleProperties", mugshotGradlePropertiesProvider)
        test.inputs.property("mugshot.layoutlib.version", NATIVE_LIB_VERSION)

        // Source dirs catch in-place content edits. PrepareResourcesTask tracks paths only and
        // its JSON output is byte-identical when contents change, so it can't invalidate the test.
        test.inputs.files(localResourceDirs)
          .withPropertyName("mugshot.localResourceDirs")
          .withPathSensitivity(PathSensitivity.RELATIVE)
        test.inputs.files(sources.moduleResourceDirs)
          .withPropertyName("mugshot.moduleResourceDirs")
          .withPathSensitivity(PathSensitivity.RELATIVE)
        test.inputs.files(sources.aarExplodedDirs)
          .withPropertyName("mugshot.aarResourceDirs")
          .withPathSensitivity(PathSensitivity.RELATIVE)
        test.inputs.files(localAssetDirs)
          .withPropertyName("mugshot.localAssetDirs")
          .withPathSensitivity(PathSensitivity.RELATIVE)
        test.inputs.files(sources.moduleAssetDirs)
          .withPropertyName("mugshot.moduleAssetDirs")
          .withPathSensitivity(PathSensitivity.RELATIVE)
        test.inputs.files(sources.aarAssetDirs)
          .withPropertyName("mugshot.aarAssetDirs")
          .withPathSensitivity(PathSensitivity.RELATIVE)

        // Declared so Test Distribution ships the file (#1790); also catches path-structure changes.
        test.inputs.file(writeResourcesTask.flatMap { it.mugshotResources })
          .withPropertyName("mugshot.test.resources")
          .withPathSensitivity(PathSensitivity.NONE)

        test.inputs.dir(snapshotOutputDir.presentWhen(isVerifyRun))
          .withPropertyName("mugshot.snapshot.input.dir")
          .withPathSensitivity(PathSensitivity.RELATIVE)
          .optional()

        test.outputs.dir(snapshotOutputDir.presentWhen(isRecordRun))
          .withPropertyName("mugshot.snapshots.output.dir")
          .optional()

        test.outputs.dir(reportOutputDir).withPropertyName("mugshot.report.dir")
        test.outputs.dir(failureDir)
          .withPropertyName("mugshot.failures.dir")
          .optional()

        test.doFirst {
          if (isVerifyRun.get()) failureDir.get().asFile.deleteRecursively()
          // Note: these are lazy properties that are not resolvable in the Gradle configuration phase.
          // They need special handling, so they're added as inputs.property above, and systemProperty here.
          test.systemProperties.putAll(mugshotGradlePropertiesProvider.get())
          test.systemProperties["mugshot.layoutlib.runtime.root"] =
            layoutlibNativeRuntimeFileCollection.singleFile.absolutePath
          test.systemProperties["mugshot.layoutlib.resources.root"] =
            layoutlibResourcesFileCollection.singleFile.absolutePath
          test.systemProperties["mugshot.test.record"] = isRecordRun.get()
          test.systemProperties["mugshot.test.record.overwriteOnMaxPercentDifference"] =
            overwriteOnMaxPercentDifferenceProvider.orNull == "true"
          test.systemProperties["mugshot.test.verify"] = isVerifyRun.get()
          test.systemProperties["mugshot.snapshot.dir"] = snapshotOutputDir.get().asFile.absolutePath
          test.systemProperties["mugshot.failures.dir"] = failureDir.get().asFile.absolutePath
        }

        test.doLast {
          val uri = reportOutputDir.get().asFile.toPath().resolve("index.html").toUri()
          test.logger.log(LIFECYCLE, "See the Mugshot report at: $uri")
        }
      }

      recordTaskProvider.configure { it.dependsOn(testTaskProvider) }
      verifyTaskProvider.configure { it.dependsOn(testTaskProvider) }
    }
  }

  private fun createDiffRegistryFactory(
    failureDirProperty: Provider<Directory>,
    isVerifyRun: Provider<Boolean>
  ): () -> Map<Pair<String, String>, DiffImage> =
    {
      val failureDir = failureDirProperty.get().asFile
      if (isVerifyRun.get() && failureDir.exists()) {
        failureDir.listFiles()
          ?.filter { it.name.startsWith("delta-") }
          ?.associate { diff ->
            // TODO: read from failure diff metadata file instead of brittle parsing
            val nameSegments = diff.name.split("_", limit = 3)
            val testClassPackage = nameSegments[0].replace("delta-", "")
            val testClass = "$testClassPackage.${nameSegments[1]}"
            val testMethodWithLabel = nameSegments[2].substringBeforeLast('.')

            Pair(testClass, testMethodWithLabel) to DiffImage(
              path = diff.path,
              mimeType = if (diff.extension == "png") "image/png" else "image/webp",
              base64EncodedImage =
              @OptIn(ExperimentalEncodingApi::class)
              Base64.encode(diff.readBytes())
            )
          } ?: emptyMap()
      } else {
        emptyMap()
      }
    }

  @DisableCachingByDefault(because = "Lifecycle task with no output; only forwards --tests")
  public abstract class MugshotTask : DefaultTask() {
    @Option(option = "tests", description = "Sets test class or method name to be included, '*' is supported.")
    public open fun setTestNameIncludePatterns(testNamePattern: List<String>): MugshotTask {
      project.tasks.withType(Test::class.java).configureEach {
        it.setTestNameIncludePatterns(testNamePattern)
      }
      return this
    }
  }

  private fun <T : AbstractTestTask> T.setTestReporter(testReporter: TestReporter) {
    AbstractTestTask::class.java
      .getDeclaredMethod("setTestReporter", TestReporter::class.java).apply {
        isAccessible = true
        invoke(this@setTestReporter, testReporter)
      }
  }

  private fun Project.setupLayoutlibRuntimeDependency(): FileCollection {
    val operatingSystem = OperatingSystem.current()
    val nativeLibraryArtifactId = when {
      operatingSystem.isMacOsX -> {
        val osArch = System.getProperty("os.arch").lowercase(Locale.US)
        if (osArch.startsWith("x86")) "mac" else "mac-arm"
      }

      operatingSystem.isWindows -> "win"
      else -> "linux"
    }

    val nativeRuntimeConfiguration = configurations.create("layoutlibRuntime")
    nativeRuntimeConfiguration.dependencies.add(
      dependencies.create("com.android.tools.layoutlib:layoutlib-runtime:$NATIVE_LIB_VERSION:$nativeLibraryArtifactId")
    )
    dependencies.registerTransform(UnzipTransform::class.java) { transform ->
      transform.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
      transform.to.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
    }

    return nativeRuntimeConfiguration
      .artifactViewFor(ArtifactTypeDefinition.DIRECTORY_TYPE)
      .files
  }

  private fun Project.setupLayoutlibResourcesDependency(): FileCollection {
    val layoutlibResourcesConfiguration = configurations.create("layoutlibResources")
    layoutlibResourcesConfiguration.dependencies.add(
      dependencies.create("com.android.tools.layoutlib:layoutlib-resources:$NATIVE_LIB_VERSION")
    )
    dependencies.registerTransform(UnzipTransform::class.java) { transform ->
      transform.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
      transform.to.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.DIRECTORY_TYPE)
    }

    return layoutlibResourcesConfiguration
      .artifactViewFor(ArtifactTypeDefinition.DIRECTORY_TYPE)
      .files
  }

  /**
   * Wires the preview processor into KSP so consumers write no `ksp` block of their own.
   *
   * Reacts to KSP rather than applying it: the plugin only declares KSP `compileOnly`, so it never
   * forces a KSP version on a project that does not want one.
   *
   * The processor is added to the per-variant `ksp<Variant>` configuration and never to the unit
   * test one. That scoping is load-bearing — the processor writes `mugshotPreviewCases` into the
   * module namespace, and running it a second time for the test compilation would declare the same
   * top level property twice.
   */
  private fun Project.configurePreviewProcessor(
    extension: AndroidComponentsExtension<*, *, *>,
    isMultiplatformProject: Boolean
  ) {
    // Kotlin Multiplatform names its source sets differently, and addTestDependency() only wires
    // the preview modules for the Android source sets, so generating a test there would not compile.
    if (isMultiplatformProject) return

    pluginManager.withPlugin(KSP_PLUGIN) {
      // Added as the configurations are created rather than from an `onVariants` callback. KSP
      // decides whether to skip its task while it walks the variants, and a callback registered
      // after KSP's own runs too late -- the task is skipped as having no processors.
      val processor = mugshotDependency("mugshot-preview-processor")
      configurations.matching { it.isMainKspConfiguration() }.all { it.dependencies.add(processor) }

      // KSP processor options are global rather than per variant, and a module has exactly one
      // namespace, so this is read from the DSL once the build script has been evaluated.
      afterEvaluate {
        val namespace = extensions.findByType(CommonExtension::class.java)?.namespace
        if (namespace == null) {
          logger.warn(
            "Mugshot could not resolve this module's namespace, so @Mugshot previews will not be " +
              "generated. Set android.namespace, or add the KSP argument " +
              "'$PREVIEW_NAMESPACE_OPTION' by hand."
          )
          return@afterEvaluate
        }
        extensions.getByType(KspExtension::class.java).arg(PREVIEW_NAMESPACE_OPTION, namespace)
      }

      // The test is only generated where the processor runs. It reads `mugshotPreviewCases`, which
      // the processor emits, so writing it into a module without KSP would not compile.
      extension.onVariants { variant ->
        val testVariant = (variant as? HasUnitTest)?.unitTest ?: return@onVariants
        val generatePreviewTests = tasks.register(
          "generateMugshot${variant.name.capitalize()}PreviewTests",
          GeneratePreviewTestTask::class.java
        ) { task ->
          task.packageName.set(variant.namespace)
        }
        val testSources = testVariant.sources.kotlin ?: testVariant.sources.java
        testSources?.addGeneratedSourceDirectory(generatePreviewTests) { it.outputDirectory }
      }
    }
  }

  /**
   * Whether this is a KSP configuration for a main compilation.
   *
   * Only a narrowing, not a guarantee: KSP's test configurations inherit the main one, so the
   * processor still reaches the unit test compilation and recognises that source set itself. This
   * keeps the declared intent clear and avoids adding the dependency twice.
   */
  private fun Configuration.isMainKspConfiguration(): Boolean {
    if (!name.startsWith("ksp")) return false
    val variantSuffix = name.removePrefix("ksp")
    return variantSuffix.isNotEmpty() && !variantSuffix.endsWith("Test")
  }

  private fun Project.mugshotDependency(artifact: String): Dependency =
    if (isInternal()) {
      dependencies.project(mapOf("path" to ":$artifact"))
    } else {
      dependencies.create("uk.co.fractalmotion.mugshot:$artifact:$VERSION")
    }

  private fun Project.addTestDependency() {
    val dependency = if (isInternal()) {
      dependencies.project(mapOf("path" to ":mugshot"))
    } else {
      dependencies.create("uk.co.fractalmotion.mugshot:mugshot:$VERSION")
    }

    val allowedConfigs = mutableSetOf<String>()

    when {
      plugins.hasPlugin(ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY_PLUGIN) -> {
        val kmp = extensions.getByType(KotlinMultiplatformExtension::class.java)
        kmp.targets.configureEach { target ->
          target.compilations.configureEach { compilation ->
            if (compilation is KotlinMultiplatformAndroidHostTestCompilation) {
              val configurationName = compilation.defaultSourceSet.implementationConfigurationName
              allowedConfigs += configurationName
              configurations.getByName(configurationName).dependencies.add(dependency)
            }
          }
        }
      }
      plugins.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN) -> {
        val kmp = extensions.getByType(KotlinMultiplatformExtension::class.java)
        with(kmp) {
          sourceSets.androidUnitTest.configure {
            val configurationName = it.implementationConfigurationName
            allowedConfigs += configurationName
            configurations.getByName(configurationName).dependencies.add(dependency)
          }
        }
      }
      else -> {
        val android = extensions.getByType(CommonExtension::class.java)
        val configurationName = android.sourceSets.getByName(TEST_SOURCE_SET_NAME).implementationConfigurationName
        allowedConfigs += configurationName
        configurations.getByName(configurationName).dependencies.add(dependency)

        // The preview pipeline, so @Mugshot needs no dependency declarations either. The
        // annotations and the generated catalogue compile into main; the bridge that turns that
        // catalogue into rules is test-only. Not wired for Kotlin Multiplatform, where source set
        // names differ -- those projects declare these by hand.
        val mainImplementation =
          android.sourceSets.getByName(MAIN_SOURCE_SET_NAME).implementationConfigurationName
        configurations.getByName(mainImplementation).dependencies.addAll(
          listOf(
            mugshotDependency("mugshot-annotations"),
            mugshotDependency("mugshot-preview-runtime")
          )
        )
        configurations.getByName(configurationName).dependencies
          .add(mugshotDependency("mugshot-preview-junit"))
      }
    }

    afterEvaluate {
      val kmp = extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return@afterEvaluate
      kmp.sourceSets.forEach { sourceSet ->
        val configName = sourceSet.implementationConfigurationName
        if (configName in allowedConfigs) return@forEach
        val config = configurations.findByName(configName) ?: return@forEach
        val hasMugshot = config.dependencies.any {
          it.group == "uk.co.fractalmotion.mugshot" && it.name == "mugshot"
        }
        check(!hasMugshot) {
          "Mugshot must not be declared in '$configName', as it should only resolve on Android JVM tests."
        }
      }
    }
  }

  @Suppress("UnstableApiUsage")
  private fun <T : Any> Provider<T>.presentWhen(condition: Provider<Boolean>): Provider<T> =
    condition.filter { it }.flatMap { this }

  /**
   * Resolves `src/test/snapshots` from the unit test source set.
   *
   * Prefers `static` over `all` deliberately. `all` includes generated source directories, and
   * Mugshot now contributes one itself (see `generateMugshot<Variant>PreviewTests`), so `all` could
   * hand back a directory under `build/` and silently relocate every golden image.
   */
  private fun Project.snapshotDir(testVariant: UnitTest): Provider<Directory> {
    val sources = testVariant.sources.kotlin
      ?: testVariant.sources.java
      ?: error("No Kotlin or Java sources on ${testVariant.name}")
    val projectDirectory = layout.projectDirectory
    // Kotlin Multiplatform's androidHostTest registers no static dirs, and never receives a
    // generated one either, so falling back to `all` there is safe.
    // `flatMap` rather than `zip`: `all` carries `generateMugshot<Variant>PreviewTests` as a
    // producer task, and querying it while the configuration cache serialises this task's
    // registered input/output properties fails before that task has run. Reaching for it only
    // when `static` is empty keeps the common path free of that dependency.
    return sources.static.flatMap { static ->
      if (static.isEmpty()) sources.all else providerFactory.provider { static }
    }.map { dirs ->
      val sourceSetRoot = dirs.firstOrNull()?.asFile?.parentFile
        ?: error("No source dirs registered for ${testVariant.name}")
      projectDirectory.dir(sourceSetRoot.path).dir("snapshots")
    }
  }

  private fun Project.isInternal(): Boolean =
    providers.gradleProperty("uk.co.fractalmotion.mugshot.internal").orNull == "true"

  private fun Project.overwriteOnMaxPercentDifferenceProvider(): Provider<String> =
    providers.gradleProperty("uk.co.fractalmotion.mugshot.overwriteOnMaxPercentDifference")

  /**
   * Resolves the `targetSdk` Mugshot writes into the test manifest.
   *
   * Prefers `android.testOptions.targetSdk` if set, otherwise the project's `compileSdk`,
   * otherwise [DEFAULT_COMPILE_SDK_VERSION]. Mirrors AGP 9's planned default behavior
   * (`BooleanOption.DEFAULT_TARGET_SDK_TO_COMPILE_SDK_IF_UNSET` in AGP sources) of
   * defaulting test rendering to `compileSdk` rather than the variant's resolved
   * `targetSdk` — which on AGP 8.x falls through to `minSdk` when
   * `defaultConfig.targetSdk` is unset, exposing the test render to a lower SDK that
   * Compose/layoutlib don't cleanly support today.
   */
  private fun Project.targetSdk(): Provider<String> =
    providerFactory.provider {
      val commonExtension = extensions.findByType(CommonExtension::class.java)
      val resolved = commonExtension?.testOptions?.targetSdk
        ?: commonExtension?.compileSdk
        ?: DEFAULT_COMPILE_SDK_VERSION
      resolved.toString()
    }

  private fun Provider<List<Directory>>?.relativize(directory: Directory): Provider<List<String>> =
    this?.map { dirs -> dirs.map { directory.relativize(it.asFile) } }
      ?: providerFactory.provider { emptyList() }
}

/** Passes absolute-path system properties as `-D` JVM args without adding them to the cache key (#1874). */
internal class MugshotSystemPropertiesArgumentProvider(
  @get:Internal val systemProperties: Provider<Map<String, String>>
) : CommandLineArgumentProvider {
  override fun asArguments(): Iterable<String> =
    systemProperties.get().entries
      .sortedBy { it.key }
      .map { (key, value) -> "-D$key=$value" }
}

private const val DEFAULT_COMPILE_SDK_VERSION = 36
private const val ANDROID_KOTLIN_MULTIPLATFORM_LIBRARY_PLUGIN = "com.android.kotlin.multiplatform.library"
private const val KSP_PLUGIN = "com.google.devtools.ksp"
private const val PREVIEW_NAMESPACE_OPTION = "uk.co.fractalmotion.mugshot.preview.namespace"
private const val KOTLIN_MULTIPLATFORM_PLUGIN = "org.jetbrains.kotlin.multiplatform"
