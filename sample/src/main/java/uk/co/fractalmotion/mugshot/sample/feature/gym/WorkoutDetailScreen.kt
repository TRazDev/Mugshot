package uk.co.fractalmotion.mugshot.sample.feature.gym

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotListRow
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotScreenScaffold
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSectionHeader
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotStatRow
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtworkBanner
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun WorkoutDetailScreen(state: WorkoutDetailUiState, modifier: Modifier = Modifier) {
  MugshotScreenScaffold(
    title = stringResource(state.nameRes),
    modifier = modifier,
    onBack = {}
  ) {
    MugshotArtworkBanner(
      icon = R.drawable.ic_dumbbell,
      contentDescription = null,
      paletteIndex = 0,
      modifier = Modifier.fillMaxWidth()
    )

    MugshotStatRow(
      stats = listOf(
        state.volume to stringResource(R.string.gym_stat_volume),
        state.duration to stringResource(R.string.gym_stat_duration),
        state.workingSets to stringResource(R.string.gym_stat_sets)
      )
    )

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
      MugshotSectionHeader(title = stringResource(R.string.gym_detail_section_exercises))
      state.exercises.forEach { exercise ->
        MugshotListRow(
          title = stringResource(exercise.nameRes),
          subtitle = stringResource(
            R.string.gym_sets_reps,
            exercise.sets,
            exercise.reps,
            exercise.weight
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    Text(
      text = stringResource(R.string.gym_plan_name),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Mugshot
@Preview
@Composable
internal fun WorkoutDetailScreenPreview() {
  MugshotTheme { WorkoutDetailScreen(state = GymFixtures.pushDay) }
}
