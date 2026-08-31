package uk.co.fractalmotion.mugshot.annotations

/**
 * Marks a `@Preview` composable for screenshot coverage.
 *
 * On its own this records a single golden image at the library's default configuration. Stack any
 * of the axis annotations in this package to widen that into a matrix — see [MugshotMatrix].
 *
 * Targets annotation classes as well as functions, so a team can bundle its own house style into
 * one name and apply that instead:
 *
 * ```
 * @Mugshot
 * @MugshotDevices(MugshotDevice.PHONE, MugshotDevice.TABLET)
 * @MugshotLightDark
 * annotation class OurScreenshots
 * ```
 *
 * The annotated function must be `@Composable`, must carry a `@Preview`, must not be `private`,
 * and must take no parameters other than a single `@PreviewParameter`.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class Mugshot
