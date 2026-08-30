package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotGradients
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotElevation
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/**
 * The sample's default container.
 *
 * Uses tonal elevation rather than a drop shadow — see [MugshotElevation] for why that matters to
 * the goldens.
 */
@Composable
internal fun MugshotCard(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.medium,
  tonalElevation: Dp = MugshotElevation.level1,
  contentPadding: Dp = MaterialTheme.spacing.large,
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier,
    shape = shape,
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    tonalElevation = tonalElevation
  ) {
    Column(modifier = Modifier.padding(contentPadding), content = content)
  }
}

/** A gradient panel for the one thing on a screen that should draw the eye first. */
@Composable
internal fun MugshotHeroCard(
  modifier: Modifier = Modifier,
  contentPadding: Dp = MaterialTheme.spacing.extraLarge,
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.large)
      .background(MugshotGradients.hero())
      .padding(contentPadding),
    content = content
  )
}
