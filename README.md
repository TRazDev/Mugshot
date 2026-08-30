Mugshot
========

![Mugshot](.github/images/logo.webp)
An Android library to render your application screens without a physical device or emulator.

```kotlin
class ProfileScreenTest {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar"
    // ...see docs for more options
  )

  @Test
  fun profile() {
    mugshot.snapshot {
      MyTheme { ProfileScreen(state = sampleProfile) }
    }
  }

  @Test
  fun profileInDarkMode() {
    mugshot.unsafeUpdateConfig(
      deviceConfig = PIXEL_6.copy(nightMode = NightMode.NIGHT)
    )
    mugshot.snapshot {
      MyTheme { ProfileScreen(state = sampleProfile) }
    }
  }
}
```

Android Views work the same way:

```kotlin
val view = mugshot.inflate<LaunchView>(R.layout.launch)
mugshot.snapshot(view)
```

See the [project website][mugshot] for documentation and APIs.

Using JUnit 5
-------

```kotlin
lateinit var mugshot: Mugshot

@BeforeEach
fun setup(testInfo: TestInfo) {
  mugshot = Mugshot().apply {
    setup(
      testName = TestName(
        packageName = testInfo.testClass.get().`package`?.name.orEmpty(),
        className = testInfo.testClass.get().simpleName,
        methodName = testInfo.testMethod.get().name
      )
    )
  }
}

@AfterEach
fun tearDown() {
  mugshot.teardown()
}

@Test
fun snapshot_example() {
  val view = mugshot.inflate<TextView>(android.R.layout.simple_list_item_1).apply {
    text = "Hello Mugshot"
    textSize = 24f
    gravity = Gravity.CENTER
  }

  mugshot.snapshot(view)
}
```

Snapshotting `@Preview` composables
-------

Annotate a `@Preview` composable in your main source set with `@Mugshot` and the
preview processor generates a `mugshotPreviews` list your tests can iterate, so a
screen's preview and its golden never drift apart:

```kotlin
@Mugshot
@Preview
@Composable
internal fun ProfileScreenPreview() {
  MyTheme { ProfileScreen(state = sampleProfile) }
}
```

```kotlin
class GeneratedPreviewTest {
  @get:Rule val mugshot = Mugshot()

  @Test
  fun preview() {
    mugshotPreviews.forEach { preview ->
      mugshot.snapshot(name = preview.snapshotName) { preview.composable() }
    }
  }
}
```

A `@PreviewParameter` is expanded at test runtime into one snapshot per value,
named by position, so a single preview can cover a screen's loading, empty and
populated states. The annotated function must be `@Composable`, must carry a
literal `@Preview`, and must not be `private`.

Wire the processor up alongside the Gradle plugin:

```groovy
apply plugin: 'com.google.devtools.ksp'

ksp {
  arg("uk.co.fractalmotion.mugshot.preview.namespace", "com.example.myapp")
}

dependencies {
  implementation "uk.co.fractalmotion.mugshot:mugshot-annotations:$version"
  implementation "uk.co.fractalmotion.mugshot:mugshot-preview-runtime:$version"
  kspDebug "uk.co.fractalmotion.mugshot:mugshot-preview-processor:$version"
  lintChecks "uk.co.fractalmotion.mugshot:mugshot-preview-lints:$version"
}
```

Tasks
-------

```bash
./gradlew :sample:testDebug
```

Runs tests and generates an HTML report at `sample/build/reports/mugshot/` showing all
test runs and snapshots.

```bash
./gradlew :sample:recordMugshotDebug
```

Saves snapshots as golden values to a predefined source-controlled location
(defaults to `src/test/snapshots`).

```bash
./gradlew :sample:verifyMugshotDebug
```

Runs tests and verifies against previously-recorded golden values. Failures generate diffs at `sample/build/mugshot/failures`.

For more examples, check out the [sample][sample] project.

Git LFS
--------
It is recommended you use [Git LFS][lfs] to store your snapshots.  Here's a quick setup:

```bash
brew install git-lfs
git config core.hooksPath  # optional, confirm where your git hooks will be installed
git lfs install --local
git lfs track "**/snapshots/**/*.webp"
git add .gitattributes
# Optional to improve git checkout performance
git config lfs.setlockablereadonly false
```

On CI, you might set up something like:

`$HOOKS_DIR/pre-receive`
```bash
# compares files that match .gitattributes filter to those actually tracked by git-lfs
diff <(git ls-files ':(attr:filter=lfs)' | sort) <(git lfs ls-files -n | sort) >/dev/null

ret=$?
if [[ $ret -ne 0 ]]; then
  echo >&2 "This remote has detected files committed without using Git LFS. Run 'brew install git-lfs && git lfs install' to install it and re-commit your files.";
  exit 1;
fi
```

`your_build_script.sh`
```bash
if [[ is running snapshot tests ]]; then
  # fail fast if files not checked in using git lfs
  "$HOOKS_DIR"/pre-receive
  git lfs install --local
  git lfs pull
fi
```

Jetifier
--------

If using Jetifier to migrate off Support libraries, add the following to your `gradle.properties` to
exclude bundled Android dependencies.

```properties
android.jetifier.ignorelist=android-base-common,common
```

Lottie
--------

When taking screenshots of Lottie animations, you need to force Lottie to not run on a background thread, otherwise Mugshot can throw exceptions [#494](https://github.com/cashapp/paparazzi/issues/494), [#630](https://github.com/cashapp/paparazzi/issues/630).

```kotlin
@Before
fun setup() {
    LottieTask.EXECUTOR = Executor(Runnable::run)
}
```

LocalInspectionMode
--------
Some Composables -- such as `GoogleMap()` -- check for `LocalInspectionMode` to short-circuit to a `@Preview`-safe Composable.

However, Mugshot does not set `LocalInspectionMode` globally to ensure that the snapshot represents the true production output, similar to how it overrides `View.isInEditMode` for legacy views.

As a workaround, we recommend wrapping such a Composable in a custom Composable with a `CompositionLocalProvider` and setting `LocalInspectionMode` there.

```kotlin
 @Test
  fun inspectionModeView() {
    mugshot.snapshot(
      CompositionLocalProvider(LocalInspectionMode provides true) {
        YourComposable()
      }
    )
  }
```

Releases
--------

Our [change log][changelog] has release history.

Using plugin application:
```groovy
buildscript {
  repositories {
    mavenCentral()
    google()
  }
  dependencies {
    classpath 'uk.co.fractalmotion.mugshot:mugshot-gradle-plugin:0.1.0'
  }
}

apply plugin: 'uk.co.fractalmotion.mugshot'
```

Using the plugins DSL:
```groovy
plugins {
  id 'uk.co.fractalmotion.mugshot' version '0.1.0'
}
```

Snapshots of the development version are available in [the Central Portal Snapshots repository][snap].

```groovy
repositories {
  // ...
  maven {
    url 'https://central.sonatype.com/repository/maven-snapshots/'
  }
}
```

Credits
-------

Mugshot is a fork of [Paparazzi][upstream], created and maintained by Square, Inc.
Essentially all of the hard engineering here — the layoutlib integration, the
resource loading, the rendering pipeline — is their work. This fork exists to take
the project in a direction that would have been too breaking to land upstream, and
is not affiliated with, endorsed by, or sponsored by Square, Inc. or Cash App.

See [NOTICE](NOTICE) for the full attribution and a summary of what has changed.

License
-------

```
Copyright 2019 Square, Inc.
Copyright 2026 Fractal Motion

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

 [mugshot]: https://trazdev.github.io/Mugshot/
 [sample]: https://github.com/TRazDev/Mugshot/tree/main/sample
 [lfs]: https://git-lfs.github.com/
 [upstream]: https://github.com/cashapp/paparazzi
 [changelog]: https://trazdev.github.io/Mugshot/changelog/
 [snap]: https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/uk/co/fractalmotion/mugshot/
