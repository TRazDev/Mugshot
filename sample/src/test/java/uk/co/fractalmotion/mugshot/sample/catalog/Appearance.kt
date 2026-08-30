package uk.co.fractalmotion.mugshot.sample.catalog

/**
 * Light or dark, as a named test parameter.
 *
 * A `Boolean` parameter would put `true`/`false` in the golden filename; this puts `LIGHT`/`DARK`.
 */
enum class Appearance(val dark: Boolean) {
  LIGHT(dark = false),
  DARK(dark = true)
}
