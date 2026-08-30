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

Annotate a `@Preview` composable with `@Mugshot` and it gets a golden image. There is no
test to write — the Gradle plugin generates one that renders every annotated preview in
the module:

```kotlin
@Mugshot
@Preview
@Composable
internal fun ProfileScreenPreview() {
  MyTheme { ProfileScreen(state = sampleProfile) }
}
```

Apply KSP alongside the plugin and that is the whole setup. The annotations, the preview
runtime and the processor are all supplied for you:

```groovy
plugins {
  id 'com.android.library'
  id 'com.google.devtools.ksp'
  id 'uk.co.fractalmotion.mugshot'
}
```

### Configuring what gets rendered

Bare `@Mugshot` records one image at the library defaults. Each additional annotation adds
an axis, and **axes multiply**:

| Annotation | Renders |
| --- | --- |
| `@MugshotShrink` | wrapped to the content, for components and dialogs |
| `@MugshotFullScreen` | the whole scrollable height in one image |
| `@MugshotDevices` | phone, foldable, tablet, landscape — or the ones you name |
| `@MugshotWear` | a round and a square watch |
| `@MugshotLightDark` | light and dark |
| `@MugshotFontScales` | 1x, 1.5x, 2x — or the ones you name |
| `@MugshotLocales("ar")` | the default locale plus each you name, mirroring RTL ones |
| `@MugshotMatrix` | devices x light/dark x font scales — 24 images |

```kotlin
@Mugshot
@MugshotDevices(MugshotDevice.PHONE, MugshotDevice.TABLET)
@MugshotLightDark
@Preview
@Composable
internal fun ProfileScreenPreview() { ... }
// 2 devices x 2 appearances = 4 golden images
```

Bundle your house style behind one name — the annotations target annotation classes too:

```kotlin
@Mugshot
@MugshotDevices(MugshotDevice.PHONE, MugshotDevice.TABLET)
@MugshotLightDark
annotation class OurScreenshots
```

A `@PreviewParameter` provider is expanded at test time into one image per value, so a
single preview can cover a screen's loading, empty and populated states.

Two things to know. Mugshot reads its configuration from these annotations and **not** from
`@Preview`'s own arguments — setting `device` or `uiMode` on `@Preview` changes what the IDE
renders without changing the golden, and lint warns when you do. And a
`@MugshotFullScreen` preview must not scroll itself: the renderer measures with an unbounded
height, which `Modifier.verticalScroll` and `Scaffold` both reject.

The annotated function must be `@Composable`, must carry a `@Preview`, must not be
`private`, and must take no parameters other than a single `@PreviewParameter`.

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
