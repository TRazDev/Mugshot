package uk.co.fractalmotion.mugshot.sample.feature.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.annotations.MugshotDevices
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotEmptyState
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotProgressRing
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotScreenScaffold
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSectionHeader
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSegmentedBar
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSparkline
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotStatTile
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun HealthScreen(state: HealthUiState, modifier: Modifier = Modifier) {
  MugshotScreenScaffold(title = stringResource(R.string.health_title), modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
      val accents = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
      )
      state.rings.forEachIndexed { index, ring ->
        MugshotProgressRing(
          progress = ring.progress,
          label = stringResource(ring.labelRes),
          value = ring.value,
          size = 108.dp,
          accent = accents[index % accents.size]
        )
      }
    }

    if (!state.hasData) {
      MugshotEmptyState(
        icon = R.drawable.ic_heart_pulse,
        title = stringResource(R.string.health_empty_title),
        description = stringResource(R.string.health_empty_detail)
      )
      return@MugshotScreenScaffold
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
      MugshotSectionHeader(title = stringResource(R.string.health_section_vitals))
      state.metrics.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
          row.forEach { metric ->
            MugshotStatTile(
              value = metric.value,
              label = stringResource(metric.labelRes),
              icon = metric.icon,
              supporting = if (metric.supportingArg != null) {
                stringResource(metric.supportingRes, metric.supportingArg)
              } else {
                stringResource(metric.supportingRes)
              },
              modifier = Modifier.weight(1f)
            )
          }
          repeat(2 - row.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
      MugshotSectionHeader(title = stringResource(R.string.health_section_sleep))
      MugshotCard(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = stringResource(
            R.string.health_sleep_total,
            state.sleepHours,
            state.sleepMinutes
          ),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        val stageColors = listOf(
          MaterialTheme.colorScheme.primary,
          MaterialTheme.colorScheme.secondary,
          MaterialTheme.colorScheme.tertiary,
          MaterialTheme.colorScheme.surfaceContainerHighest
        )
        MugshotSegmentedBar(
          segments = state.sleepStages.mapIndexed { index, stage ->
            stage.fraction to stageColors[index % stageColors.size]
          }
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
          state.sleepStages.forEachIndexed { index, stage ->
            SleepLegend(
              label = stringResource(stage.labelRes),
              color = stageColors[index % stageColors.size]
            )
          }
        }
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
      MugshotSectionHeader(title = stringResource(R.string.health_section_trend))
      MugshotCard(modifier = Modifier.fillMaxWidth()) {
        MugshotSparkline(values = state.restingHeartRateTrend)
      }
    }
  }
}

@Composable
private fun SleepLegend(label: String, color: Color, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
  ) {
    Text(text = "•", style = MaterialTheme.typography.labelMedium, color = color)
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

/** Today's rings against a watch that has not been worn yet. */
internal class HealthStateProvider : PreviewParameterProvider<HealthUiState> {
  override val values = sequenceOf(HealthFixtures.today, HealthFixtures.noData)
}

@Mugshot
@MugshotDevices
@Preview
@Composable
internal fun HealthScreenPreview(@PreviewParameter(HealthStateProvider::class) state: HealthUiState) {
  MugshotTheme { HealthScreen(state = state) }
}
