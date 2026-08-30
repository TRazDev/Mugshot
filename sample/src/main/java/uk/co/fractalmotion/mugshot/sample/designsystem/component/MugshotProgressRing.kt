package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/**
 * An activity ring.
 *
 * Drawn with [Canvas] rather than assembled from Material parts so the sweep, cap and track weight
 * are all under the design system's control.
 *
 * Only the value sits inside the ring; the label goes underneath. Putting both inside looks tidier
 * at the default text size and then breaks out of the circle at larger ones — which is exactly what
 * the font scale snapshot test caught.
 */
@Composable
internal fun MugshotProgressRing(
  progress: Float,
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  size: Dp = 132.dp,
  strokeWidth: Dp = 12.dp,
  accent: Color = MaterialTheme.colorScheme.primary
) {
  val track = MaterialTheme.colorScheme.surfaceContainerHighest
  Column(
    modifier = modifier.width(size),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
      Canvas(modifier = Modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val inset = stroke.width / 2f
        val arcSize = Size(this.size.width - stroke.width, this.size.height - stroke.width)
        drawArc(
          color = track,
          startAngle = 0f,
          sweepAngle = 360f,
          useCenter = false,
          topLeft = Offset(inset, inset),
          size = arcSize,
          style = stroke
        )
        drawArc(
          color = accent,
          startAngle = -90f,
          sweepAngle = 360f * progress.coerceIn(0f, 1f),
          useCenter = false,
          topLeft = Offset(inset, inset),
          size = arcSize,
          style = stroke
        )
      }
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
  }
}
