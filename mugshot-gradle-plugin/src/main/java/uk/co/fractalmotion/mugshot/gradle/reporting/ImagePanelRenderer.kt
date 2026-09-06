package uk.co.fractalmotion.mugshot.gradle.reporting

import org.gradle.internal.html.SimpleHtmlWriter
import org.gradle.reporting.ReportRenderer

/**
 * Renders a failed snapshot as three labelled columns: the golden, the difference, and what this
 * run rendered.
 *
 * The difference sits in the middle so it is next to both of the images it was computed from.
 *
 * Panels that do not exist are skipped, so a snapshot with no golden yet shows the render alone
 * rather than an empty column.
 */
internal class ImagePanelRenderer : ReportRenderer<DiffImage, SimpleHtmlWriter>() {
  override fun render(images: DiffImage, htmlWriter: SimpleHtmlWriter) {
    val panels = listOfNotNull(images.reference, images.diff, images.actual)
    if (panels.isEmpty()) return

    // Wrapped in a span to work around a CSS problem in IE, inherited from Gradle's own report.
    htmlWriter
      .startElement("span")
      .startElement("table")
      .attribute("style", "table-layout: fixed; width: 100%")

    htmlWriter.startElement("thead").startElement("tr")
    panels.forEach { panel ->
      htmlWriter
        .startElement("th")
        .attribute("style", "width: ${100 / panels.size}%; text-align: left; padding: 0.5em 1em")
        .characters(panel.name)
        .endElement()
    }
    htmlWriter.endElement().endElement() // tr, thead

    // The grid shows through anything transparent, which is how a render with no background
    // stays distinguishable from a white one.
    htmlWriter.startElement("tbody").attribute("class", "grid").startElement("tr")
    panels.forEach { panel ->
      htmlWriter
        .startElement("td")
        .attribute("style", "padding: 1em; vertical-align: top")
        .startElement("img")
        .attribute("src", panel.dataUri)
        .attribute("style", "max-width: 100%; height: auto")
        .attribute("alt", "${panel.name} image")
        .endElement() // img
        .endElement() // td
    }
    htmlWriter.endElement().endElement() // tr, tbody

    htmlWriter
      .endElement() // table
      .endElement() // span
  }
}
