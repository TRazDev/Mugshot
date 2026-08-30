package uk.co.fractalmotion.mugshot.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.junit.Test
import java.io.File

/**
 * Resource and asset plumbing across local, module and AAR dependencies.
 *
 * See [MugshotPluginTestCase] for the shared fixture helpers.
 */
@Suppress("ktlint:standard:max-line-length")
class ResourcesAssetsTest : MugshotPluginTestCase() {
  @Test
  fun verifyResourcesGeneratedForJavaProject() {
    val fixtureRoot = fixture("verify-resources-java")

    val result = fixtureRoot.runBuild(":consumer:testDebug")

    assertThat(result.task(":consumer:prepareMugshotDebugResources")).isNotNull()

    val resourcesFile = File(fixtureRoot, "consumer/build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()

    val config = resourcesFile.loadConfig()
    assertThat(config.mainPackage).isEqualTo("uk.co.fractalmotion.mugshot.plugin.test")
    assertThat(config.resourcePackageNames).containsExactly(
      "uk.co.fractalmotion.mugshot.plugin.test",
      "com.example.mylibrary",
      "uk.co.fractalmotion.mugshot.plugin.test.module1",
      "uk.co.fractalmotion.mugshot.plugin.test.module2"
    )
    assertThat(config.projectResourceDirs).containsExactly(
      "src/main/res",
      "src/debug/res",
      "build/generated/res/extra"
    )
    assertThat(config.moduleResourceDirs).containsExactly(
      "../module1/build/intermediates/packaged_res/debug/packageDebugResources",
      "../module2/build/intermediates/packaged_res/debug/packageDebugResources"
    )
    assertThat(config.aarExplodedDirs)
      .comparingElementsUsing(MATCHES_PATTERN)
      .containsExactly("$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/external/res\$")
  }

  @Test
  fun verifyResourcesGeneratedForKotlinProject() {
    val fixtureRoot = fixture("verify-resources-kotlin")

    val result = fixtureRoot.runBuild(":consumer:testDebug")

    assertThat(result.task(":consumer:prepareMugshotDebugResources")).isNotNull()

    val resourcesFile = File(fixtureRoot, "consumer/build/intermediates/mugshot/debug/resources.json")
    assertThat(resourcesFile.exists()).isTrue()

    val config = resourcesFile.loadConfig()
    assertThat(config.mainPackage).isEqualTo("uk.co.fractalmotion.mugshot.plugin.test")
    assertThat(config.resourcePackageNames).containsExactly(
      "uk.co.fractalmotion.mugshot.plugin.test",
      "com.example.mylibrary",
      "uk.co.fractalmotion.mugshot.plugin.test.module1",
      "uk.co.fractalmotion.mugshot.plugin.test.module2"
    )
    assertThat(config.projectResourceDirs).containsExactly(
      "src/main/res",
      "src/debug/res",
      "build/generated/res/extra"
    )
    assertThat(config.moduleResourceDirs).containsExactly(
      "../module1/build/intermediates/packaged_res/debug/packageDebugResources",
      "../module2/build/intermediates/packaged_res/debug/packageDebugResources"
    )
    assertThat(config.aarExplodedDirs)
      .comparingElementsUsing(MATCHES_PATTERN)
      .containsExactly("$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/external/res\$")
  }

  @Test
  fun verifyResourcesUpdatedWhenLocalResourceChanges() {
    val fixtureRoot = fixture("verify-update-local-resources-change")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val valuesDir = File(fixtureRoot, "src/main/res/values").registerForDeletionOnExit()
    val destResourceFile = File(valuesDir, "colors.xml")
    val firstResourceFile = File(fixtureRoot, "src/test/resources/colors1.xml")
    val secondResourceFile = File(fixtureRoot, "src/test/resources/colors2.xml")

    // Original resource
    firstResourceFile.copyTo(destResourceFile, overwrite = false)

    val firstRun = fixtureRoot.runBuild("testDebug", "--build-cache") { forwardOutput() }

    firstRun.assertTaskSucceeded(":prepareMugshotDebugResources")
    firstRun.assertTaskSucceeded(":testDebugUnitTest")

    val resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")

    var config = resourcesFile.loadConfig()
    assertThat(config.projectResourceDirs).containsExactly(
      "src/main/res",
      "src/debug/res"
    )

    buildDir.deleteRecursively()

    // Update resource
    secondResourceFile.copyTo(destResourceFile, overwrite = true)

    val secondRun = fixtureRoot.runBuild(":testDebug", "--build-cache") { forwardOutput() }

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE) // paths didn't change
    secondRun.assertTaskSucceeded(":testDebugUnitTest") // but contents did

    config = resourcesFile.loadConfig()
    assertThat(config.projectResourceDirs).containsExactly(
      "src/main/res",
      "src/debug/res"
    )
  }

  @Test
  fun verifyResourcesUpdatedWhenModuleResourceChanges() {
    val fixtureRoot = fixture("verify-update-module-resources-change")
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val consumerModuleRoot = File(fixtureRoot, "consumer")
    val buildDir = consumerModuleRoot.resolve("build").registerForDeletionOnExit()

    val producerModuleRoot = File(fixtureRoot, "producer")
    val valuesDir = File(producerModuleRoot, "src/main/res/values").registerForDeletionOnExit()
    val destResourceFile = File(valuesDir, "colors.xml")
    val firstResourceFile = File(producerModuleRoot, "src/test/resources/colors1.xml")
    val secondResourceFile = File(producerModuleRoot, "src/test/resources/colors2.xml")

    // Original resource
    firstResourceFile.copyTo(destResourceFile, overwrite = false)

    val firstRun = fixtureRoot.runBuild(":consumer:testDebug", "--build-cache") { forwardOutput() }

    firstRun.assertTaskSucceeded(":consumer:prepareMugshotDebugResources")
    firstRun.assertTaskSucceeded(":consumer:testDebugUnitTest")

    val resourcesFile = File(consumerModuleRoot, "build/intermediates/mugshot/debug/resources.json")

    var config = resourcesFile.loadConfig()
    assertThat(config.moduleResourceDirs)
      .containsExactly("../producer/build/intermediates/packaged_res/debug/packageDebugResources")

    buildDir.deleteRecursively()

    // Update resource
    secondResourceFile.copyTo(destResourceFile, overwrite = true)

    val secondRun = fixtureRoot.runBuild(":consumer:testDebug", "--build-cache") { forwardOutput() }

    secondRun.assertTaskOutcome(":consumer:prepareMugshotDebugResources", FROM_CACHE) // paths didn't change
    secondRun.assertTaskSucceeded(":consumer:testDebugUnitTest") // but contents did

    config = resourcesFile.loadConfig()
    assertThat(config.moduleResourceDirs)
      .containsExactly("../producer/build/intermediates/packaged_res/debug/packageDebugResources")
  }

  @Test
  fun verifyResourcesUpdatedWhenExternalDependencyChanges() {
    val fixtureRoot = fixture("verify-update-aar-resources-change")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    System.setProperty("isFirstRun", "true")

    val firstRun = fixtureRoot.runBuild(":prepareMugshotDebugResources", "--build-cache") { forwardOutput() }

    firstRun.assertTaskSucceeded(":prepareMugshotDebugResources")

    val resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")

    var config = resourcesFile.loadConfig()
    assertThat(config.aarExplodedDirs)
      .comparingElementsUsing(MATCHES_PATTERN)
      .containsExactly(
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/external1/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/core-1.17.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/annotation-experimental-1.4.1/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/core-viewtree-1.0.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/lifecycle-runtime-2.6.2/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/profileinstaller-1.3.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/startup-runtime-1.1.1/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/tracing-1.2.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/core-runtime-2.2.0/res\$"
      )

    buildDir.deleteRecursively()

    System.setProperty("isFirstRun", "false")

    val secondRun = fixtureRoot.runBuild(":prepareMugshotDebugResources", "--build-cache") { forwardOutput() }

    secondRun.assertTaskSucceeded(":prepareMugshotDebugResources")

    config = resourcesFile.loadConfig()
    assertThat(config.aarExplodedDirs)
      .comparingElementsUsing(MATCHES_PATTERN)
      .containsExactly(
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/external2/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/core-1.17.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/annotation-experimental-1.4.1/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/core-viewtree-1.0.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/lifecycle-runtime-2.6.2/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/profileinstaller-1.3.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/startup-runtime-1.1.1/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/tracing-1.2.0/res\$",
        "$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/core-runtime-2.2.0/res\$"
      )
  }

  @Test
  fun verifyAssetsUpdatedWhenLocalAssetChanges() {
    val fixtureRoot = fixture("verify-update-local-assets-change")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val assetsDir = File(fixtureRoot, "src/main/assets").registerForDeletionOnExit()
    val destAssetFile = File(assetsDir, "secret.txt")
    val firstAssetFile = File(fixtureRoot, "src/test/resources/secret1.txt")
    val secondAssetFile = File(fixtureRoot, "src/test/resources/secret2.txt")

    // Original asset
    firstAssetFile.copyTo(destAssetFile, overwrite = false)

    val firstRun = fixtureRoot.runBuild("testDebug", "--build-cache") { forwardOutput() }

    firstRun.assertTaskSucceeded(":prepareMugshotDebugResources")

    firstRun.assertTaskSucceeded(":testDebugUnitTest")

    val resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")

    var config = resourcesFile.loadConfig()
    assertThat(config.projectAssetDirs).containsExactly("src/main/assets", "src/debug/assets")

    buildDir.deleteRecursively()

    // Update asset
    secondAssetFile.copyTo(destAssetFile, overwrite = true)

    val secondRun = fixtureRoot.runBuild(":testDebug", "--build-cache") { forwardOutput() }

    secondRun.assertTaskOutcome(":prepareMugshotDebugResources", FROM_CACHE) // paths didn't change

    secondRun.assertTaskSucceeded(":testDebugUnitTest") // but contents did

    config = resourcesFile.loadConfig()
    assertThat(config.projectAssetDirs).containsExactly("src/main/assets", "src/debug/assets")
  }

  @Test
  fun verifyAssetsUpdatedWhenModuleAssetChanges() {
    val fixtureRoot = fixture("verify-update-module-assets-change")
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    val consumerModuleRoot = File(fixtureRoot, "consumer")
    val buildDir = consumerModuleRoot.resolve("build").registerForDeletionOnExit()

    val producerModuleRoot = File(fixtureRoot, "producer")
    val assetsDir = File(producerModuleRoot, "src/main/assets").registerForDeletionOnExit()
    val destAssetFile = File(assetsDir, "secret.txt")
    val firstAssetFile = File(producerModuleRoot, "src/test/resources/secret1.txt")
    val secondAssetFile = File(producerModuleRoot, "src/test/resources/secret2.txt")

    // Original asset
    firstAssetFile.copyTo(destAssetFile, overwrite = false)

    val firstRun = fixtureRoot.runBuild(":consumer:testDebug", "--build-cache") { forwardOutput() }

    firstRun.assertTaskSucceeded(":consumer:prepareMugshotDebugResources")

    firstRun.assertTaskSucceeded(":consumer:testDebugUnitTest")

    val resourcesFile = File(consumerModuleRoot, "build/intermediates/mugshot/debug/resources.json")

    var config = resourcesFile.loadConfig()
    assertThat(config.projectAssetDirs).containsExactly(
      "src/main/assets",
      "src/debug/assets",
      "../producer/build/intermediates/assets/debug/mergeDebugAssets"
    )

    buildDir.deleteRecursively()

    // Update asset
    secondAssetFile.copyTo(destAssetFile, overwrite = true)

    val secondRun = fixtureRoot.runBuild(":consumer:testDebug", "--build-cache") { forwardOutput() }

    secondRun.assertTaskOutcome(":consumer:prepareMugshotDebugResources", FROM_CACHE) // paths didn't change

    secondRun.assertTaskSucceeded(":consumer:testDebugUnitTest") // but contents did

    config = resourcesFile.loadConfig()
    assertThat(config.projectAssetDirs).containsExactly(
      "src/main/assets",
      "src/debug/assets",
      "../producer/build/intermediates/assets/debug/mergeDebugAssets"
    )
  }

  @Test
  fun verifyAssetsUpdatedWhenExternalDependencyChanges() {
    val fixtureRoot = fixture("verify-update-aar-assets-change")
    val buildDir = fixtureRoot.resolve("build").registerForDeletionOnExit()
    fixtureRoot.resolve("build-cache").registerForDeletionOnExit()

    System.setProperty("isFirstRun", "true")

    val firstRun = fixtureRoot.runBuild(":prepareMugshotDebugResources", "--build-cache") { forwardOutput() }

    firstRun.assertTaskSucceeded(":prepareMugshotDebugResources")

    val resourcesFile = File(fixtureRoot, "build/intermediates/mugshot/debug/resources.json")

    var config = resourcesFile.loadConfig()
    assertThat(config.aarAssetDirs)
      .comparingElementsUsing(MATCHES_PATTERN)
      .containsExactly("$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/external1/assets\$")

    buildDir.deleteRecursively()

    System.setProperty("isFirstRun", "false")

    val secondRun = fixtureRoot.runBuild(":prepareMugshotDebugResources", "--build-cache") { forwardOutput() }

    secondRun.assertTaskSucceeded(":prepareMugshotDebugResources")

    config = resourcesFile.loadConfig()
    assertThat(config.aarAssetDirs)
      .comparingElementsUsing(MATCHES_PATTERN)
      .containsExactly("$GRADLE_CACHE_TRANSFORMS_PATH_REGEX/external2/assets\$")
  }

  @Test
  fun verifyOpenAssets() = fixture("open-assets").buildSucceeds("consumer:testDebug")

  @Test
  fun resourceMultiModule() {
    val fixtureRoot = fixture("resource-multi-module")

    fixtureRoot.runBuild("verifyMugshotDebug") { forwardOutput() }
  }

  @Test
  fun transitiveResources() = fixture("transitive-resources").buildSucceeds("module:verifyMugshotDebug")
}
