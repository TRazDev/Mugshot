package uk.co.fractalmotion.mugshot.gradle.reporting

internal data class DiffImage(
  val path: String, // TODO relative path
  val mimeType: String,
  val base64EncodedImage: String
) {
  val text: String
    get() = "Error displaying image for $path"
}
