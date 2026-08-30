package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/** A labelled column chart — weekly training volume, daily steps, and so on. */
@Composable
internal fun MugshotBarChart(
  values: List<Float>,
  labels: List<String>,
  modifier: Modifier = Modifier,
  height: Dp = 132.dp,
  accent: Color = MaterialTheme.colorScheme.primary,
  highlightIndex: Int = -1
) {
  val peak = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
  val track = MaterialTheme.colorScheme.surfaceContainerHighest
  Column(modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(height),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
      verticalAlignment = Alignment.Bottom
    ) {
      values.forEachIndexed { index, value ->
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          contentAlignment = Alignment.BottomCenter
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight()
              .clip(MaterialTheme.shapes.small)
              .background(track)
          )
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(value / peak)
              .clip(MaterialTheme.shapes.small)
              .background(if (index == highlightIndex) accent else accent.copy(alpha = 0.55f))
          )
        }
      }
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(MaterialTheme.spacing.extraLarge),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
      verticalAlignment = Alignment.CenterVertically
    ) {
      labels.forEach { label ->
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    }
  }
}

/** A trend line with no axes — a shape, not a data table. */
@Composable
internal fun MugshotSparkline(
  values: List<Float>,
  modifier: Modifier = Modifier,
  height: Dp = 56.dp,
  accent: Color = MaterialTheme.colorScheme.primary
) {
  Canvas(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
  ) {
    if (values.size < 2) return@Canvas
    val minimum = values.min()
    val maximum = values.max()
    val range = (maximum - minimum).takeIf { it > 0f } ?: 1f
    val stepX = size.width / (values.size - 1)
    val path = Path()
    values.forEachIndexed { index, value ->
      val x = stepX * index
      val y = size.height - ((value - minimum) / range) * size.height
      if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path = path, color = accent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
  }
}

/** A stacked proportion bar — sleep stages, macro split, storage breakdown. */
@Composable
internal fun MugshotSegmentedBar(
  segments: List<Pair<Float, Color>>,
  modifier: Modifier = Modifier,
  height: Dp = 14.dp
) {
  val total = segments.sumOf { it.first.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .clip(MaterialTheme.shapes.extraLarge)
  ) {
    segments.forEach { (value, color) ->
      Box(
        modifier = Modifier
          .weight(value / total)
          .fillMaxHeight()
          .background(color)
      )
    }
  }
}
