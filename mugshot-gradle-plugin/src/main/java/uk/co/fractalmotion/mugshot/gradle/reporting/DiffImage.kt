package uk.co.fractalmotion.mugshot.gradle.reporting

/** One image of a failure comparison, ready to embed in the report. */
internal data class ReportImage(
  val name: String,
  val mimeType: String,
  val base64EncodedImage: String
) {
  val dataUri: String get() = "data:$mimeType;base64, $base64EncodedImage"
}

/**
 * The panels shown for a failed snapshot: the golden, what this run rendered, and the difference
 * between them.
 *
 * Any of them can be missing. A snapshot that has never been recorded has no reference, and a
 * render that failed before producing an image has neither of the other two.
 */
internal data class DiffImage(
  val reference: ReportImage?,
  val actual: ReportImage?,
  val diff: ReportImage?
) {
  val isEmpty: Boolean get() = reference == null && actual == null && diff == null
}
