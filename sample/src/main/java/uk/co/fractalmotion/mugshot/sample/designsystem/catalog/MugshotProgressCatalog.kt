package uk.co.fractalmotion.mugshot.sample.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.annotations.MugshotLightDark
import uk.co.fractalmotion.mugshot.annotations.MugshotShrink
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotBarChart
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotProgressRing
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSegmentedBar
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSparkline
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun MugshotProgressCatalog(modifier: Modifier = Modifier) {
  MugshotCatalogPage(title = "Progress and charts", modifier = modifier) {
    MugshotCatalogRow(label = "Activity rings") {
      Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        MugshotProgressRing(
          progress = 0.72f,
          label = "Move",
          value = "72%",
          size = 104.dp,
          accent = MaterialTheme.colorScheme.primary
        )
        MugshotProgressRing(
          progress = 0.45f,
          label = "Exercise",
          value = "45%",
          size = 104.dp,
          accent = MaterialTheme.colorScheme.secondary
        )
        MugshotProgressRing(
          progress = 0.93f,
          label = "Stand",
          value = "93%",
          size = 104.dp,
          accent = MaterialTheme.colorScheme.tertiary
        )
      }
    }
    MugshotCatalogRow(label = "Bar chart, Thursday highlighted") {
      MugshotBarChart(
        values = listOf(24f, 38f, 12f, 47f, 30f, 18f, 41f),
        labels = listOf("M", "T", "W", "T", "F", "S", "S"),
        highlightIndex = 3
      )
    }
    MugshotCatalogRow(label = "Sparkline") {
      MugshotSparkline(values = listOf(12f, 18f, 9f, 22f, 26f, 19f, 31f, 28f, 36f))
    }
    MugshotCatalogRow(label = "Segmented bar") {
      MugshotSegmentedBar(
        segments = listOf(
          0.42f to MaterialTheme.colorScheme.primary,
          0.28f to MaterialTheme.colorScheme.secondary,
          0.18f to MaterialTheme.colorScheme.tertiary,
          0.12f to MaterialTheme.colorScheme.surfaceContainerHighest
        )
      )
    }
  }
}

@Mugshot
@MugshotShrink
@MugshotLightDark
@Preview
@Composable
internal fun MugshotProgressCatalogPreview() {
  MugshotTheme { MugshotProgressCatalog() }
}
