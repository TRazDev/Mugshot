# Change Log

Mugshot is a fork of [Paparazzi](https://github.com/cashapp/paparazzi). Its own
release history starts here; Paparazzi's history is preserved verbatim further
down, under "Upstream Paparazzi history".

## [Unreleased]

## [3.1.0] - 2026-09-03

### Fixed
* `@MugshotLocales` did not infer right-to-left layout from Android's BCP 47 locale qualifiers.
  The language subtag was read as everything before the first `-`, so a `b+`-prefixed qualifier
  such as `b+ar+u+nu+arab` was never matched against the right-to-left languages and rendered
  left to right. That form matters because it is the only way to pin a locale's numbering system:
  without it, digit shapes in formatted strings come from the host JDK's CLDR data and change
  between JDK releases, so the same preview yields different goldens on different JDKs.

## [3.0.2] - 2026-09-01

### Fixed
* Mugshot tasks failed when Gradle's configuration cache was enabled, with
  `Querying the mapped value of and(property 'static', property 'all') before task
  ':<module>:generateMugshot<Variant>PreviewTests' has completed is not supported`. Resolving the
  snapshot directory zipped the unit test source set's `static` and `all` directories, and `all`
  carries the generated-test task as a producer, so serialising the task graph read it before that
  task had run. The fallback to `all` is now reached only when `static` is empty, as its
  documentation always claimed. Consumers no longer need `org.gradle.configuration-cache=false`.

## [3.0.1] - 2026-08-31

### Added
* Screenshot coverage from a single annotation. A `@Preview` composable marked `@Mugshot` now gets
  a golden image with no test file to write: the Gradle plugin generates a parameterised JUnit test
  that renders every annotated preview in the module, one case each so a failure names the preview
  that broke. Apply KSP alongside the plugin and the annotations, preview runtime and processor are
  supplied for you — the `ksp` block, the processor dependency and the namespace argument are gone.
* Axis annotations that configure what gets rendered, and multiply when stacked: `@MugshotShrink`,
  `@MugshotFullScreen`, `@MugshotDevices`, `@MugshotWear`, `@MugshotLightDark`,
  `@MugshotFontScales`, `@MugshotLocales` and `@MugshotMatrix`. All of them target annotation
  classes as well as functions, so a team can bundle its house style behind one name. RTL layout is
  inferred from the locale.
* `mugshot-preview-junit`, a new artifact holding the bridge from a generated preview case to a
  `Mugshot` rule. It is added to `testImplementation` by the plugin.
* Lint warning `MugshotPreviewArgumentsIgnored`, reported when a `@Mugshot` function's `@Preview`
  sets configuration Mugshot does not read, so the IDE preview and the golden cannot silently
  diverge.

* `@Mugshot` previews may take a `@PreviewParameter`. The processor cannot enumerate a
  `PreviewParameterProvider` at compile time, so generated code defers to `parameterizedFrames` in
  `mugshot-preview-runtime`, which expands the provider's values when the test runs. Each value
  becomes its own image, named by position (`_0`, `_1`, ...) rather than by the value, so golden
  filenames stay deterministic. The `PreviewParametersNotSupported` lint check is gone.

### Fixed
* A `@Mugshot` function carrying more than one `@Preview` generated one entry per `@Preview`, each
  with an identical `snapshotName`. No `@Preview` argument is read, so those entries were
  indistinguishable and their goldens collided. One entry is now generated per function.
* The `PreviewAnnotationNotFound` lint check only looked at direct annotations while the processor
  resolved `@Preview` recursively. A `@Mugshot` function reaching `@Preview` through a custom
  multi-preview annotation generated correctly and then failed lint. The check now walks annotation
  trees the same way the processor does.

### Changed
* **Breaking:** `mugshot-preview-runtime` now exposes `MugshotPreviewCase` and
  `MugshotPreviewConfig` in place of `MugshotPreviewData`, and `parameterizedFrames` in place of
  `parameterizedPreviews`. The generated property is `mugshotPreviewCases`, not `mugshotPreviews`.
  Hand-written tests that iterated the old list can be deleted — the plugin generates one.
* **Breaking:** `@Preview` arguments are not read. Configuration comes from the Mugshot annotations
  instead; see the lint warning above.
* **Breaking:** the project is renamed from Paparazzi to Mugshot. There is no
  compatibility layer — every name below changes at once:
  * Maven group `app.cash.paparazzi` -> `uk.co.fractalmotion.mugshot`, and the
    artifacts `paparazzi`, `paparazzi-gradle-plugin`, `paparazzi-annotations`,
    `paparazzi-preview-{runtime,processor,lints}` gain the `mugshot` prefix.
  * Gradle plugin id `app.cash.paparazzi` -> `uk.co.fractalmotion.mugshot`.
  * Kotlin package `app.cash.paparazzi.**` -> `uk.co.fractalmotion.mugshot.**`.
  * Types `Paparazzi` -> `Mugshot`, `PaparazziSdk` -> `MugshotSdk`, and the
    `@Paparazzi` annotation -> `@Mugshot`.
  * Tasks `recordPaparazzi*`, `verifyPaparazzi*`, `cleanRecordPaparazzi*`,
    `deletePaparazziSnapshots` -> the same names with `Mugshot`.
  * System properties `paparazzi.*` -> `mugshot.*`, and Gradle properties
    `app.cash.paparazzi.*` -> `uk.co.fractalmotion.mugshot.*`.
  * Output directories `build/reports/paparazzi` -> `build/reports/mugshot` and
    `build/paparazzi/failures` -> `build/mugshot/failures`.

  Golden snapshot filenames embed the test's package name, so renaming your own
  test packages renames your goldens. Rename the files rather than re-recording
  if you want to prove the rebrand changed no pixels.
* **Breaking:** snapshots are now stored as lossless WebP (`.webp`) instead of PNG. Re-record your
  goldens with `./gradlew recordMugshot<Variant>`; existing `.png` goldens are not read.

### Removed
* **Breaking:** removed accessibility snapshot rendering and validation.
* **Breaking:** removed animated snapshot support. `Mugshot#gif` and `MugshotSdk#gif` are gone,
  along with the `snapshots/videos` directory. Mugshot now records a single frame per snapshot.
  Use `Mugshot#snapshot(view, offsetMillis)` to capture a specific point in an animation, or
  `InstantAnimationsRule` to snapshot an animation's terminal state.
* **Breaking:** `SnapshotHandler#newFrameHandler` no longer takes `frameCount` and `fps`. Custom
  `SnapshotHandler` implementations must drop those parameters.

---

# Upstream Paparazzi history

Everything below is Paparazzi's change log as of the fork point, Copyright 2019
Square, Inc. Version links point at the upstream repository.

## [2.0.0-alpha05] - 2026-05-20

This release supports pre-AGP 9.0 consumers.

### New
* Add support for the new Android library multiplatform plugin (#2115, #2332)
* Add `setup`/`teardown` hooks on Paparazzi to decouple from JUnit 4; document usage with JUnit Jupiter (#2209, #2333)
* LayoutLib v16.2.1
* Compose 1.11.2
* [Gradle Plugin] Gradle 9.3.1
* [Gradle Plugin] Tidy task wiring and inputs (#2205, #2334)

### Fixed
* Fix configuration-cache issue resolving `project` in provider mapping (#2241)
* Fix `NoClassDefFoundError` on `org/gradle/reporting/HtmlWriterTools` to support Gradle 8.x users (#2316)
* Destroy lifecycle owner after snapshots (#2325)
* Fix Paparazzi dependency scope for KMP Android tests (#2330)

Kudos to @geoff-powell, @nishatoma, @colinmarsch, @oldergod, @eboudrant, @tcmulcahy and others for contributions this release!

## [2.0.0-alpha04] - 2026-01-20

As of this release, consumers must build on Java 21+ environments.

### New
* Allow differ to be configured (#2001)
* Introduce additional differ types (#2009)
* Introduce defaultLocale system property (#2203)
* Update DeviceConfigs (#2176)
* Add support for overwriting snapshots when max percent diff threshold is reached (#2067)
* LayoutLib v16.1.1
* Compose 1.10.1
* Kotlin 2.3.0
* [Gradle Plugin] Gradle 9.2.1
* [Gradle Plugin] Android Gradle Plugin 8.13.2

### Fixed
* Add fix for boundsInWindow incorrectly calculating position on screen (#1983)
* Fix font loading on Windows (#2074)
* Fail test if exception is thrown during effect (#2214)
* Fix differ comparison between black pixels with 100% alpha and black pixels with 0% alpha (#2078)
* Adjust apng failure precision (#2186)
* Make plugin compatible with Gradle Isolated Projects (#2154)

### Changed
* Migrate to Dokka2 (#1783)

Kudos to @geoff-powell, @colinmarsch, @SimonMarquis, @nishatoma, @joshskeen, @kboyarshinov and others for contributions this release!

## [2.0.0-alpha03] - 2026-01-20

Ignore; use 2.0.0-alpha04 instead.

## [2.0.0-alpha02] - 2025-06-20

### New
* Bump default compileSdk to API 36
* LayoutLib v15.2.3
* Compose 1.8.3
* Kotlin 2.1.21
* [Gradle Plugin] Gradle 8.14.2
* [Gradle Plugin] Android Gradle Plugin 8.10.1

### Fixed
* Generate build failures even if golden images are not present
* Fully advance choreographer correctly for Compose

### Changed
- In-development snapshots are now published to the Central Portal Snapshots repository at https://central.sonatype.com/repository/maven-snapshots/.

Kudos to @geoff-powell, @colinmarsch, @SimonMarquis and others for contributions this release!

## [2.0.0-alpha01] - 2025-04-15

### New
* Add support for compose views that are hidden or invisible
* Add gradle property to control default max percent difference
* LayoutLib v15.1.4
* [Gradle Plugin] Gradle 8.13
* [Gradle Plugin] Android Gradle Plugin 8.8.1

### Fixed
* Fix default layout params for compose snapshots
* Fix issue where unmergedNode has 0 elements
* Use compileOnly for kotlin/agp plugins
* Bypass font path prefix check in ResourcesCompat::loadFont

Kudos to @geoff-powell, @colinmarsch, @darshanparajuli, @DSteve595, @adamalyyan and others for contributions this release!

## [1.3.5] - 2024-11-06

### New
* Render pending recompositions for `@Composable`s that require a second layout pass
* Include failure delta image in JUnit test reporting
* Migrate Paparazzi to layoutlib Jellyfish 2023.3.1
* Compose 1.7.5
* Kotlin 2.0.21
* [Gradle Plugin] Gradle 8.10.2
* [Gradle Plugin] Android Gradle Plugin 8.4.2

### Fixed
* Improve Gradle test task caching by preventing overlapping outputs with snapshotOutputDir
* Migrate plugin to use modern AGP variant APIs
* Fix support for AndroidX ResourcesCompat.getFont()
* Fix inconsistent cross-platform text renderings in failure delta image
* Relax image comparisons with OffByTwo differ to work around cross-platform rendering issues
* Avoid invalid chars in Windows filenames
* Fix file move failures on Windows
* Avoid hash collisions when images have similar RGB content
* Cleanup unnecessary "loadPublicResourceNames" warning from log output

Kudos to @geoff-powell, @colinmarsch, @BrianGardnerAtl, @ribafish, @gabrielittner and others for contributions this release!

## [1.3.4] - 2024-05-23

### New
* Support for animated-PNG-based snapshots using Paparazzi#gif
* New tasks! deletePaparazziSnapshots and cleanRecordPaparazzi${VARIANT} clear orphaned snapshots
* Add boolean flag to decide if image should be scaled or full-sized
* Migrate Paparazzi to layoutlib Iguana 2023.2.1
* Compose 1.5.14
* Kotlin 1.9.24
* [Gradle Plugin] Gradle 8.7
* [Gradle Plugin] Android Gradle Plugin 8.3.2

### Fixed
* Include resource references from generated resource folders
* Fix gradle caching for resources coming from aar dependencies
* Support SHRINK render mode when using unsafeUpdateConfig
* Fix issue where multiple snapshots fail when using render extensions
* Remove guava workaround from [1.3.2] now that Collector APIs are in guava-android

Kudos to @geoff-powell, @gamepro65, @kevinzheng-ap, @nak5ive, @TWiStErRob, @emuguy1 and others for contributions this release!

## [1.3.3] - 2024-03-01

### New
* Migrate Paparazzi to layoutlib Hedgehog 2023.1.1
* Compose 1.5.8
* Kotlin 1.9.22
* [Gradle Plugin] Gradle 8.6
* [Gradle Plugin] Android Gradle Plugin 8.2.1

### Fixed
* Fix variant caching issues in new resource/asset loading mechanisms
* Remove legacy resources/assets loading mechanism
* Set HardwareConfig width and height based on orientation
* Apply round screen qualifier to device config
* Restrict Paparazzi's public API
* Remove obsolete NEXUS_5_LAND DeviceConfig
* Fix formatting so that all digits show upon failure
* Stop resolving dependencies at configuration time
* Use our own internal HandlerDispatcher for Compose Ui tests
* Include generated string resources
* Reset logger to prevent swallowing exceptions

Kudos to @gamepro65, @kevinzheng-ap, @BrianGardnerAtl, @adamalyyan, and others for contributions this release!

## [1.3.2] - 2024-01-13

### NOTE: Due to a known issue with [how Guava now publishes its artifact](https://github.com/google/guava/issues/6567), you will need to apply the following snippet workaround to your root build.gradle:
```
subprojects {
  plugins.withId("app.cash.paparazzi") {
    // Defer until afterEvaluate so that testImplementation is created by Android plugin.
    afterEvaluate {
      dependencies.constraints {
        add("testImplementation", "com.google.guava:guava") {
          attributes {
            attribute(
              TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
              objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM)
            )
          }
          because("LayoutLib and sdk-common depend on Guava's -jre published variant." +
            "See https://github.com/cashapp/paparazzi/issues/906.")
        }
      }
    }
  }
}
```
See also: https://github.com/google/guava/issues/6801.

### New
* Support for pseudolocalization tests!  To get started:
```agsl
@RunWith(TestParameterInjector::class)
class PseudolocalizationTest(
  @TestParameter locale: Locale
) {
  @get:Rule val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5.copy(locale = locale.tag)
  )

  @Test fun test() {
    paparazzi.snapshot { SomeComposable() }
  }

  enum class Locale(val tag: String?) {
    Default(null),
    Accent("en-rXA"),
    Bidi("ar-rXB")
  }
}
```

* Migrate Paparazzi to layoutlib Giraffe 2022.3.1
* Compose 1.5.0
* Kotlin 1.9.0
* [Gradle Plugin] Gradle 8.5
* [Gradle Plugin] Android Gradle Plugin 8.1.1

### Fixed
* Fix relativePath bug in port of ResourceFile
* Resolve report dir from ReportingExtension instead of hardcoding
* Make report folder variant-aware
* Remove reliance on kotlinx.coroutines.main.delay
* Use a class file locator that queries the system class loader
* Filter out unrecognized java-symbol tag warning
* Skip synthetic fields in R classes
* Update task inputs for resources and assets to account for file renames and moves
* Update delta images to support showing diff when width and height differ

Kudos to @kevinzheng-ap, @TWiStErRob, @gamepro65, @adamalyyan, @larryng, and others for contributions this release!

## [1.3.1] - 2023-07-18

### New
* Migrated to new resource and asset loading mechanisms.  To explicitly opt-out and fall back to the
legacy mechanisms, add either/both of the following to your `gradle.properties`:
```
app.cash.paparazzi.legacy.resource.loading=true
app.cash.paparazzi.legacy.asset.loading=true
```

* The Android system ui (status + navigation bar) is now hidden by default; to re-enable:
```
  @get:Rule
  val paparazzi = Paparazzi(
    showSystemUi = true
  )
```

* Relocate failure deltas from `PROJECT_ROOT/out/failures/` to `BUILD_DIR/paparazzi/failures/`
* Support for application and dynamic feature modules
* [Gradle Plugin] Gradle 8.2.1

### Fixed
* Fixes compose alert dialogs not rendering when using RenderingMode.SHRINK

Kudos to @kevinzheng-ap, @adamalyyan and others for contributions this release!

## [1.3.0] - 2023-05-31

As of this release, consumers must build on Java 17+ environments.

### New
* Migrate Paparazzi to layoutlib Flamingo 2022.2.1
* Compose 1.4.7
* Kotlin 1.8.21
* [Gradle Plugin] Gradle 8.1.1
* [Gradle Plugin] Android Gradle Plugin 8.0.2

### Fixed
* Configure android.os.Build values via reflection
* Make sure changes to system properties actually affect test tasks
* Fix caching bug with preparePaparazziResources task
* Use Dispatchers.Main for delay functionality
* Recomposition does not happen unless lifecycle is RESUMED
* Fix NPE when unit test variant is disabled
* Fix incompatibility with androidx.savedstate:1.1.0

Kudos to @gamepro65, @geoff-powell, @TWiStErRob, @adamalyyan and others for contributions this release!

## [1.2.0] - 2023-01-18

### New
* Migrate Paparazzi to layoutlib Electric Eel 2022.1.1
* Add support for RenderingMode.SHRINK to allow view-only screenshots
* Expose flag to show/hide system ui
* Register a default OnBackPressedDispatcherOwner if its present in classpath
* Bump default compileSdk to API 33
* Compose 1.3.1
* Kotlin 1.7.20
* [Gradle Plugin] Gradle 7.6
* [Gradle Plugin] Android Gradle Plugin 7.4.0

### Fixed
* Flush errors on unsafeUpdateConfig
* Only apply wear circle shape to full device screenshots
* Synchronize access to Handler_Delegate.queue
* Apply compose hooks to all snapshot calls
* Register LifecycleOwner and SavedStateRegistryOwner to all views
* Execute Handler callbacks after snapshots to clean up Compose references
* Fix RecyclerView issue due to layoutlib Dolphin update
* Keep AGP and tools dependencies aligned

Kudos to @gamepro65, @saket, @rharter and others for contributions this release!

## [1.1.0] - 2022-10-12

### New
* Migrate Paparazzi to layoutlib Chipmunk 2021.2.1
* Add support for multiplatform plugin
* Add support for JDKs 16+
* Add support for locales and layout direction (LTR/RTL)
* Add Pixel 6 & Pixel 6 Pro device configs
* Enable night mode for legacy views and composables
* Enable ui mode to support form factors other than phones/tablets, e.g., auto, watches, etc.
* Google Wear DeviceConfig support
* Expose an API for offsetting frame capture time
* Add InstantAnimationsRule to delay snapshot capture until the last frame.
* Compose 1.3.0
* Kotlin 1.7.10
* [Gradle Plugin] Gradle 7.5.1

### Fixed
* Generate resource ids to support aapt inline resources in composables
* Reset AndroidUiDispatcher between compose snapshots
* Fix OOM error when a large number of compose snapshots are verified
* Fix HTML report in development mode
* Honor customization of Gradle's build output directory
* [Gradle Plugin] Configure native platform transformed path directly in test task to reduce cache misses
* [Gradle Plugin] Fix accidental eager task creation reducing memory pressure
* [Gradle Plugin] Fail explicitly when applying Android application plugin

Kudos to @chris-horner, @swankjesse, @yschimke, @dniHze, @TWiStErRob, @gamepro65, @liutikas and others for contributions this release!

## [1.0.0] - 2022-06-03

### New
* Support for Composable snapshots
* Migrate Paparazzi to layoutlib Bumblebee 2021.1.1 for better rendering and API 31 support
* Update Paparazzi configuration via new `unsafeUpdateConfig` method instead of `snapshot`/`gif`
* Cache Paparazzi bootstrap logic for better performance per test suite
* Surface internally thrown exceptions from layoutlib
* Throw a more helpful exception if Android platform is missing
* Bump default compileSdk to API 31
* Compose 1.1.1
* Kotlin 1.6.10
* [Gradle Plugin] Gradle 7.4.2
* [Gradle Plugin] Android Gradle Plugin 7.1.2

### Fixed
* Prepend paths with file:// for clickable error output in IDE
* Update default SDK path on Linux
* Fixes crash when using InputMethodManager to show/hide keyboard
* Temporarily work around Compose runtime memory leaks
* [Gradle Plugin] Prefer namespace DSL declaration over manifest package declaration
* [Gradle Plugin] Publish plugin marker on snapshot builds
* [Gradle Plugin] Exclude androidTest sourceSets during paparazzi runs

Kudos to @luis-cortes, @nak5ive, @alexvanyo, @gamepro65 and others for contributions this release!

## [0.9.3] - 2022-01-20

### Fixed
* Load the correct mac arm artifact on M1 machines

Kudos to @geoff-powell, @nicbell for their contributions this release!


## [0.9.2] - 2022-01-20

Please ignore this release


## [0.9.1] - 2022-01-14

### Fixed
* Download mac arm artifact if on M1 machines
* Support for assets from transitive dependencies
* Add fix for ClassNotFoundException when using nonTransitiveRClass
* Update RELEASING notes to publish plugin marker artifact

Kudos to @luis-cortes, @geoff-powell, @autonomousapps and @LuK1709 for their contributions this release!


## [0.9.0] - 2021-11-22

### New
* Migrate Paparazzi to layoutlib Arctic Fox 2020.3.1, providing native support for M1 machines
* Migrate Paparazzi to layoutlib 4.2, unlocking future Compose support
* Add support for projects enabling non-transitive resources
* RenderExtension now visits view hierarchy pre-rendering instead of layering bitmaps post-rendering
* Fail-fast when Bridge.init fails, usually due to native crash
* Expose RenderingMode as a configurable option
* Bump default compileSdk to API 30
* Improve Java-interoperability experience
* Kotlin 1.5.31

### Fixed
* Don't require Android plugin to be declared before Paparazzi plugin
* Clear AnimationHandler leak after each snapshot
* Don't generate empty mov files on snapshot failure
* Add Kotlin platform bom to prevent classpath conflicts during test builds
* Use correct default Android SDK path on Windows
* Use platform-agnostic file paths in Gradle artifacts to support remote caching across platforms
* Use platform-agnostic file paths in Javascript for web page support on Windows

Kudos to @luis-cortes, @geoff-powell and @TWiStErRob for their contributions this release!


## [0.8.0] - 2021-10-07

### New
* Migrate Paparazzi to use native layoutlib for better rendering and API 30 support
* Add support for fontScale in DeviceConfig
* Add device config for Pixel 5
* Add tasks to Gradle task verification group
* Migrate publishing to gradle-maven-publish-plugin
* Migrate builds to Github Actions
* Migrate sample test from Burst to TestParameterInjector
* Kotlin 1.5.21
* [Gradle Plugin] Support for configuration caching
* [Gradle Plugin] Gradle 7.2

### Fixed
* Add method interceptor for matrix multiplication operations
* Don't swallow FileNotFoundExceptions when overridden platform dir doesn't exist
* [Gradle Plugin] Fix remote caching bug by referencing relative, not absolute, paths in intermediate resources file.

## [0.7.1] - 2021-05-17

### New
* [Gradle Plugin] Support the --tests option for record/verify tasks

### Fixed
* [Gradle Plugin] Defer task configuration until created

## [0.7.0] - 2021-02-26

### New
* Kotlin 1.4.30
* Add support for inline complex XML resources
* Enable [Burst](https://github.com/square/burst) support
* Expose maximum percentage difference in image verification as a setting
* Render extension api to add extra information to snapshots
* Allow selection of night mode in DeviceConfig
* [Gradle Plugin] Gradle 6.8.3
* [Gradle Plugin] Creating an umbrella task to execute on all variants

### Fixed
* Properly execute Choreographer.doFrame after view has been laid out
* Fix broken text appearances when style resource names contain periods
* Fix ability to access asset files
* Use target-sdk to simulate device when available
* Always write screenshots to disk in record mode
* Don't crash when running on Java 12+
* [Gradle Plugin] Force test re-runs when a resource or asset has changed
* [Gradle Plugin] Force test re-runs if generated report or snapshot dirs are deleted

## [0.6.0] - 2020-10-02

As of this release, consumers must build on Java 11 environments.

### New
* Point to a more recent version of layoutlib that runs on Android Q and builds with Java 11.
* Refactor Paparazzi to better support non-Gradle builds
* Added device configs for Pixel 4 series

## [0.5.2] - 2020-09-17

### Fixed
* [Gradle Plugin] Fixed record and verify tasks in multi-module projects.

## [0.5.1] - 2020-09-17

### Fixed
* [Gradle Plugin] Fixed race condition in record and verify tasks.

## [0.5.0] - 2020-09-16

* Initial release.



[Unreleased]: https://github.com/TRazDev/Mugshot/compare/3.1.0...HEAD
[3.1.0]: https://github.com/TRazDev/Mugshot/releases/tag/3.1.0
[3.0.2]: https://github.com/TRazDev/Mugshot/releases/tag/3.0.2
[3.0.1]: https://github.com/TRazDev/Mugshot/releases/tag/3.0.1
[2.0.0-alpha05]: https://github.com/cashapp/paparazzi/releases/tag/2.0.0-alpha05
[2.0.0-alpha04]: https://github.com/cashapp/paparazzi/releases/tag/2.0.0-alpha04
[2.0.0-alpha03]: https://github.com/cashapp/paparazzi/releases/tag/2.0.0-alpha03
[2.0.0-alpha02]: https://github.com/cashapp/paparazzi/releases/tag/2.0.0-alpha02
[2.0.0-alpha01]: https://github.com/cashapp/paparazzi/releases/tag/2.0.0-alpha01
[1.3.5]: https://github.com/cashapp/paparazzi/releases/tag/1.3.5
[1.3.4]: https://github.com/cashapp/paparazzi/releases/tag/1.3.4
[1.3.3]: https://github.com/cashapp/paparazzi/releases/tag/1.3.3
[1.3.2]: https://github.com/cashapp/paparazzi/releases/tag/1.3.2
[1.3.1]: https://github.com/cashapp/paparazzi/releases/tag/1.3.1
[1.3.0]: https://github.com/cashapp/paparazzi/releases/tag/1.3.0
[1.2.0]: https://github.com/cashapp/paparazzi/releases/tag/1.2.0
[1.1.0]: https://github.com/cashapp/paparazzi/releases/tag/1.1.0
[1.0.0]: https://github.com/cashapp/paparazzi/releases/tag/1.0.0
[0.9.3]: https://github.com/cashapp/paparazzi/releases/tag/0.9.3
[0.9.2]: https://github.com/cashapp/paparazzi/releases/tag/0.9.2
[0.9.1]: https://github.com/cashapp/paparazzi/releases/tag/0.9.1
[0.9.0]: https://github.com/cashapp/paparazzi/releases/tag/0.9.0
[0.8.0]: https://github.com/cashapp/paparazzi/releases/tag/0.8.0
[0.7.1]: https://github.com/cashapp/paparazzi/releases/tag/0.7.1
[0.7.0]: https://github.com/cashapp/paparazzi/releases/tag/0.7.0
[0.6.0]: https://github.com/cashapp/paparazzi/releases/tag/0.6.0
[0.5.2]: https://github.com/cashapp/paparazzi/releases/tag/0.5.2
[0.5.1]: https://github.com/cashapp/paparazzi/releases/tag/0.5.1
[0.5.0]: https://github.com/cashapp/paparazzi/releases/tag/0.5.0
