package uk.co.fractalmotion.mugshot.sample.feature.gym

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotBarChart
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotHeroCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotListRow
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotPrimaryButton
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotScreenScaffold
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSectionHeader
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtwork
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun GymScreen(state: GymUiState, modifier: Modifier = Modifier) {
  MugshotScreenScaffold(title = stringResource(R.string.gym_title), modifier = modifier) {
    MugshotHeroCard {
      Text(
        text = stringResource(state.planNameRes),
        style = MaterialTheme.typography.labelLarge,
        color = Color.White.copy(alpha = 0.85f)
      )
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
      Text(
        text = stringResource(state.streakHeadlineRes),
        style = MaterialTheme.typography.displaySmall,
        color = Color.White
      )
      Text(
        text = stringResource(state.streakDetailRes),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f)
      )
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
      MugshotPrimaryButton(text = stringResource(R.string.action_start_workout), onClick = {})
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
      MugshotSectionHeader(title = stringResource(R.string.gym_section_volume))
      MugshotCard(modifier = Modifier.fillMaxWidth()) {
        MugshotBarChart(
          values = state.volumeByWeek,
          labels = state.volumeWeekLabels,
          highlightIndex = state.volumeByWeek.lastIndex
        )
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
      MugshotSectionHeader(title = stringResource(R.string.gym_section_upcoming))
      state.sessions.forEach { session ->
        MugshotListRow(
          title = stringResource(session.nameRes),
          subtitle = stringResource(session.detailRes),
          leading = {
            MugshotArtwork(
              icon = session.icon,
              contentDescription = null,
              paletteIndex = session.paletteIndex,
              glyphSize = 22.dp,
              modifier = Modifier.size(46.dp)
            )
          }
        )
      }
    }
  }
}

@Mugshot
@Preview
@Composable
internal fun GymScreenPreview() {
  MugshotTheme { GymScreen(state = GymFixtures.sample) }
}
