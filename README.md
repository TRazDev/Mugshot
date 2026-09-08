Mugshot
========

[![Maven Central](https://img.shields.io/maven-central/v/uk.co.fractalmotion.mugshot/mugshot?label=Maven%20Central)](https://central.sonatype.com/artifact/uk.co.fractalmotion.mugshot/mugshot)

![Mugshot](.github/images/logo.webp)
An Android library to render your application screens without a physical device or emulator.

### 1. Add the plugin

To a module that already renders Compose previews:

```groovy
plugins {
  id 'com.google.devtools.ksp'
  id 'uk.co.fractalmotion.mugshot'
}
```

### 2. Annotate a preview

```kotlin
@Mugshot
@Preview
@Composable
internal fun ProfileScreenPreview()
```

### 3. Record

```bash
./gradlew recordMugshotDebug
```

That's it. The image is in `src/test/snapshots/images/`, and `./gradlew verifyMugshotDebug`
now fails if that screen ever changes.

No test class, no rule, no `snapshot()` call: the plugin generated the test that renders
every annotated preview in the module. Want more than one image? Add an axis — `@MugshotLightDark`
gives you light and dark, `@MugshotDevices` gives you one per device shape, and they
multiply.

Annotations
-------

`@Mugshot` marks a preview for snapshotting and records one image at the library defaults.
Every other annotation adds an axis:

| Annotation | Renders |
| --- | --- |
| `@Mugshot` | one image at the defaults — required on every snapshotted preview |
| `@MugshotShrink` | wrapped to the content, for components and dialogs |
| `@MugshotFullScreen` | the whole scrollable height in one image |
| `@MugshotDevices` | `PHONE`, `FOLDABLE`, `TABLET`, `LANDSCAPE` — or the ones you name |
| `@MugshotWear` | a round and a square watch |
| `@MugshotLightDark` | light and dark |
| `@MugshotFontScales` | `1f`, `1.5f`, `2f` — or the ones you name |
| `@MugshotLocales("ar")` | the default locale plus each you name, mirroring RTL ones |
| `@MugshotMatrix` | devices × light/dark × font scales — 24 images |

`MugshotDevice` is one of `PHONE`, `FOLDABLE`, `TABLET`, `LANDSCAPE`, `WEAR_ROUND`,
`WEAR_SQUARE`.

### Axes multiply

Each annotation is an independent axis, and the images are their cross-product. Narrow a
matrix by passing arguments rather than by dropping an annotation:

```kotlin
@Mugshot
@MugshotDevices(MugshotDevice.PHONE, MugshotDevice.TABLET)
@MugshotLightDark
@Preview
@Composable
internal fun ProfileScreenPreview() { ... }
// 2 devices × 2 appearances = 4 images
```

`@MugshotLocales` is the one axis that keeps a baseline of its own: the others already
include theirs (`PHONE`, light, `1f`), so naming a single locale gives you two images — the
default and that locale.

### Bundling

The annotations target annotation classes as well as functions, so a team can put its house
style behind one name:

```kotlin
@Mugshot
@MugshotDevices(MugshotDevice.PHONE, MugshotDevice.TABLET)
@MugshotLightDark
annotation class OurScreenshots

@OurScreenshots
@Preview
@Composable
internal fun ProfileScreenPreview() { ... }
```

### Preview parameters

A `@PreviewParameter` provider is expanded when the test runs, one image per value, so a
single preview covers a screen's loading, empty and populated states:

```kotlin
@Mugshot
@Preview
@Composable
internal fun StorefrontScreenPreview(
  @PreviewParameter(StorefrontStateProvider::class) state: StorefrontUiState
) {
  MyTheme { StorefrontScreen(state) }
}
```

Images are indexed (`_0`, `_1`, …) rather than named after the value, because a value's
`toString()` is not safe in a filename.

### Rules

An annotated function must be `@Composable`, must carry a `@Preview`, must not be `private`,
and must take no parameters other than a single `@PreviewParameter`.

A preview that breaks one of those rules is skipped. There is no test file to fail, so the build
stays green and no image appears. The lint checks turn that silence into a message:

```groovy
dependencies {
  lintChecks 'uk.co.fractalmotion.mugshot:mugshot-preview-lints:3.2.1'
}
```

| Check | Severity | Reports |
| --- | --- | --- |
| `ComposableAnnotationNotFound` | error | `@Mugshot` on a function that is not `@Composable` |
| `PreviewAnnotationNotFound` | error | `@Mugshot` with no `@Preview`, including one reached through a multi-preview annotation |
| `PrivatePreviewDetected` | error | `@Mugshot` on a private composable, which the generated test cannot call |
| `MugshotPreviewArgumentsIgnored` | warning | `@Preview` setting configuration Mugshot does not read |

The warning is the one worth having even when everything works. Setting `device`, `uiMode`,
`locale` or `fontScale` on `@Preview` changes what the IDE renders while the golden stays the
same, so your preview and your test drift apart with nothing to say so.

One thing that surprises people: **`@Preview`'s own arguments are ignored.** Mugshot takes its
configuration from the annotations above, so setting `device`, `uiMode`, `locale` or `fontScale`
on `@Preview` changes what the IDE renders without changing the golden.

[LIMITATIONS.md](LIMITATIONS.md) covers what Mugshot does not do and where its rendering differs
from a device.

### Where the images go

The generated test is `MugshotGeneratedPreviewTest`, in your module's namespace, so goldens
are named:

```
<namespace>_MugshotGeneratedPreviewTest_snapshot[<preview>_<axes>].webp
```

for example
`com.example.myapp_MugshotGeneratedPreviewTest_snapshot[ui_ProfileScreen_ProfileScreenPreview_Dark].webp`.

Tasks
-------

Each task has an anchor form that covers every variant and a per-variant form
(`recordMugshotDebug`, `verifyMugshotRelease`, and so on).

| Task | Does |
| --- | --- |
| `recordMugshot` | writes golden images to `src/test/snapshots` |
| `verifyMugshot` | renders and compares against the goldens |
| `cleanRecordMugshot` | deletes the goldens, then records |
| `deleteMugshotSnapshots` | deletes the goldens |

```bash
./gradlew recordMugshotDebug
./gradlew verifyMugshotDebug
./gradlew verifyMugshotDebug --tests '*ProfileScreen*'
```

Every run writes an HTML report to `build/reports/tests/<testTask>`. A failed snapshot appears
there with three images side by side: the golden, the difference between them, and what this run
rendered. The same images are written to `build/mugshot/failures` for CI to collect, along
with a single combined image the console error links to.

In the difference image, **ruby red** marks a pixel the two renders disagree on and everything
else is white, so the edges of the render stay visible. A pixel that matches and a pixel that
differs by little enough to pass look the same, because neither is something to act on.

To gate CI on your goldens:

```groovy
tasks.named("check").configure {
  dependsOn("verifyMugshot")
}
```

Configuration
-------

Set these in `gradle.properties`; the plugin forwards them to the test JVM.

| Property | Default | Does |
| --- | --- | --- |
| `uk.co.fractalmotion.mugshot.downscale` | `3` | render at 1/N of the device's resolution; `1` renders at full size |
| `uk.co.fractalmotion.mugshot.differ` | `offbytwo` | image comparison: `offbytwo`, `pixelperfect` |
| `uk.co.fractalmotion.mugshot.maxPercentDifferenceDefault` | `0.01` | how much difference a verification tolerates |
| `uk.co.fractalmotion.mugshot.defaultLocale` | unset | locale for every snapshot, e.g. `fr-rFR` |
| `uk.co.fractalmotion.mugshot.overwriteOnMaxPercentDifference` | `false` | rewrite goldens that differ within the threshold |

### Resolution

Snapshots render a third of the device's resolution by default. Dimensions, dpi and density
scale together, so every dp stays a dp and the layout is identical to the full-size device --
there are simply fewer pixels in it. On a 30-module project of 7176 images that made
verification 23% faster and the goldens 31% smaller, and text comes out slightly sharper,
since nothing is resampled after rendering.

`uk.co.fractalmotion.mugshot.downscale=1` renders at the device's own resolution. That is
the most detail available, so lower values are rejected. Two reasons to reach for it:

- **Density-qualified resources.** The qualifier resolves from the scaled density, so a module
  shipping `drawable-xxhdpi` PNGs can select a different asset than the real device would.
  Vector and Compose UIs are unaffected.
- **Small text you need to read** in a failure report, where a third of the pixels is a third
  of the glyph.

Changing it changes every image, so re-record when you do.

Beyond annotations
-------

Some things the annotations do not reach. For those, drive the rule yourself:

```kotlin
class ProfileScreenTest {
  @get:Rule
  val mugshot = Mugshot(
    deviceConfig = DeviceConfig.PIXEL_6,
    theme = "android:Theme.Material.Light.NoActionBar",
    showSystemUi = true
  )

  @Test
  fun profile() {
    mugshot.snapshot { MyTheme { ProfileScreen(state = sampleProfile) } }
  }
}
```

Reachable only this way: `unsafeUpdateConfig` to change device, theme or rendering mode
part-way through a test; a custom `RenderExtension` to decorate every snapshot;
`showSystemUi`, `downscale` and `maxPercentDifference`; and Android Views, via
`mugshot.inflate<MyView>(R.layout.my_view)` and `mugshot.snapshot(view)`. For JUnit 5, build
the rule yourself and call `setup(TestName(...))` and `teardown()` around each test.

The [sample][sample] project's `screen/` and `component/` test packages have worked examples
of each.

What Mugshot needs from a module
--------

**A module cannot hold both Mugshot and Robolectric tests.** Mugshot loads layoutlib's native
library, patches `Build.VERSION`, and permanently redefines `android.view.View.isInEditMode` in
the test JVM through a Byte Buddy agent. Robolectric's own native setup then fails with
`UnsatisfiedLinkError`. This applies to any Robolectric test in the module, including ones that
take no screenshots at all, so moving to Mugshot is all or nothing per module. A Robolectric
test that has to stay belongs in a module of its own.

**Tests must not run concurrently inside one JVM.** Layoutlib is initialised once and reused,
which is most of why a suite is quick, and the renderer that holds it is shared by every test in
the JVM. Gradle's default of a worker per fork is fine, and so is `maxParallelForks`, which forks
more JVMs. Running tests concurrently *within* one JVM, with JUnit 5's parallel execution for
instance, is not: they will render over each other.

Golden image names
--------

A golden is named after the test that took it: the package, the class, the method, and the label
if `snapshot` was given one. Nothing about those is bounded, and a name has to survive a
filesystem, so a name longer than 200 characters keeps its readable beginning and ends in a hash
of the whole of it:

```
com.example.feature_VeryLongTest_aVeryLongMethodName...~3f9c1a7b2e04.webp
```

The hash comes from the full name, so it is the same on every machine and every run, and two
long names that begin alike stay apart. Real names are nowhere near the limit, the longest in
this repository's own sample is 136 characters, so this only affects names that would otherwise
be rejected.

Windows caps a whole path at 260 characters unless long paths are turned on. A deep module tree
can reach that even with a name under the limit, so on Windows it is worth enabling long path
support in both the OS and Git:

```bash
git config --global core.longpaths true
```

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
    classpath 'uk.co.fractalmotion.mugshot:mugshot-gradle-plugin:3.2.1'
  }
}

apply plugin: 'uk.co.fractalmotion.mugshot'
```

Using the plugins DSL:
```groovy
plugins {
  id 'uk.co.fractalmotion.mugshot' version '3.2.1'
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

 [sample]: https://github.com/TRazDev/Mugshot/tree/main/sample
 [lfs]: https://git-lfs.github.com/
 [upstream]: https://github.com/cashapp/paparazzi
 [changelog]: https://github.com/TRazDev/Mugshot/blob/main/CHANGELOG.md
 [snap]: https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/uk/co/fractalmotion/mugshot/
