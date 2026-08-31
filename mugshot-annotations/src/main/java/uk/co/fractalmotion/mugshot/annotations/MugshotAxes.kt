package uk.co.fractalmotion.mugshot.annotations

/**
 * Renders wrapped to the content rather than filling a device.
 *
 * The right choice for components and dialogs: a golden of a button should be a button, not a
 * button in the corner of an otherwise empty phone screen. It also keeps the image small.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotShrink

/**
 * Renders the whole scrollable height in one image instead of clipping at the bottom of the device.
 *
 * Note that a screen snapshotted this way must not scroll itself — the renderer measures with an
 * unbounded height, which `Modifier.verticalScroll` and `Scaffold` both refuse. Either the app
 * scrolls the content or the renderer expands to fit it, never both.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotFullScreen

/**
 * Renders on each of the given device shapes.
 *
 * Defaults to the four that between them catch most responsive breakage: a phone, an unfolded
 * foldable, a tablet, and a phone in landscape.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotDevices(
  vararg val devices: MugshotDevice = [
    MugshotDevice.PHONE,
    MugshotDevice.FOLDABLE,
    MugshotDevice.TABLET,
    MugshotDevice.LANDSCAPE
  ]
)

/**
 * Renders on a round and a square watch.
 *
 * Separate from [MugshotDevices] on purpose. A phone layout squeezed onto a watch tells you
 * nothing, so this marks composables actually designed for the shape. On a round device the frame
 * is clipped to a circle, so the content should fill it.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotWear

/**
 * Renders in both light and dark.
 *
 * Puts the renderer into night configuration, so `isSystemInDarkTheme()` flips and `-night`
 * resource qualifiers resolve — the theme is not forced from the outside.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotLightDark

/**
 * Renders at each of the given text scales.
 *
 * `2f` is the one that earns its keep: it is where fixed heights clip, single-line labels collide
 * and a row of stats stops fitting.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotFontScales(
  vararg val scales: Float = [1f, 1.5f, 2f]
)

/**
 * Renders in each of the given locales.
 *
 * Right-to-left layout is inferred: naming an RTL locale mirrors the layout as well as swapping the
 * strings. The two pseudolocales need no translation at all — `en-rXA` pads and accents every
 * string, `ar-rXB` mirrors it — but both only transform string *resources*, so a hardcoded literal
 * will pass straight through untouched.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotLocales(
  vararg val locales: String
)

/**
 * The lot: [MugshotDevices] x [MugshotLightDark] x [MugshotFontScales], at their defaults.
 *
 * That is 4 x 2 x 3 = **24 golden images per preview**. Axes multiply, so reach for the individual
 * annotations and their arguments before reaching for this one.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class MugshotMatrix
