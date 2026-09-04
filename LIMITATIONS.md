# Limitations

What Mugshot does not do, and where its rendering differs from a device. Each entry says what
happens, why, and what to do instead.

## Not supported

### Accessibility snapshots

Paparazzi renders an accessibility overlay and validates it. Mugshot does neither, and there is no
replacement. This was removed deliberately rather than left unimplemented.

### Animated snapshots

There is no `gif`, no `snapshots/videos` directory, and `SnapshotHandler#newFrameHandler` takes no
`frameCount` or `fps`. Mugshot records one frame per snapshot.

To capture a moment in an animation, render at a chosen time:

```kotlin
mugshot.snapshot(view, offsetMillis = 2_000L)
```

To capture an animation's end state, use `InstantAnimationsRule`. What you cannot record is the
frames in between.

## Rendering differences

### Frame callbacks

A Choreographer callback does not see the same view state it would on a device.

| When the callback is posted | On a device | In Mugshot |
| --- | --- | --- |
| From `onAttachedToWindow` | runs on the next frame, after layout | runs before layout, so the view is `0x0` |
| After layout has begun, such as from `onDraw` | runs on the next frame | never runs |

Mugshot renders a single frame. `Choreographer_Delegate.doFrame` is called before the render, which
is what measure and layout happen inside, so a callback queued at attach is drained too early and
one queued during drawing has no later frame to land in.

Raising `offsetMillis` does not help. It changes what the clock reads within the frame, not which
side of layout the callback falls on.

This affects code that posts a frame callback and reads layout-dependent state inside it, such as a
custom view starting a size-dependent animation from `onAttachedToWindow`. Reading a view's size
during measure, layout or draw is unaffected, which covers nearly all UI code. Compose is also
unaffected, because a second render already runs whenever the recomposer reports pending work.

If you hit this, move the work into `onDraw` or into a layout pass rather than a frame callback.

### LocalInspectionMode is not set

Some composables, `GoogleMap()` among them, check `LocalInspectionMode` to short-circuit to a
preview-safe version. Mugshot leaves it unset so a snapshot shows true production output, the same
reasoning behind overriding `View.isInEditMode` for legacy views.

Provide it yourself where you need it:

```kotlin
@Mugshot
@Preview
@Composable
internal fun MapPreview() {
  CompositionLocalProvider(LocalInspectionMode provides true) {
    YourComposable()
  }
}
```

### A `@MugshotFullScreen` preview must not scroll itself

That mode measures with an unbounded height so it can draw the whole screen in one image, which
`Modifier.verticalScroll` and `Scaffold` both reject. Either the app scrolls the content or the
renderer expands to fit it, not both.

## Third party libraries

### Lottie

Force Lottie onto the calling thread, or Mugshot can throw:

```kotlin
@Before
fun setup() {
  LottieTask.EXECUTOR = Executor(Runnable::run)
}
```

Background see [#494](https://github.com/cashapp/paparazzi/issues/494) and
[#630](https://github.com/cashapp/paparazzi/issues/630).

### Jetifier

If you use Jetifier to migrate off Support libraries, exclude Mugshot's bundled Android
dependencies in `gradle.properties`:

```properties
android.jetifier.ignorelist=android-base-common,common
```
