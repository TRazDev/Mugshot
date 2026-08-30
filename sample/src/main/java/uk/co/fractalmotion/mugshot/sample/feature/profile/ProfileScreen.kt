package uk.co.fractalmotion.mugshot.sample.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.annotations.Mugshot
import uk.co.fractalmotion.mugshot.sample.R
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotHeroCard
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotListRow
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotScreenScaffold
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotSectionHeader
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotStatusChip
import uk.co.fractalmotion.mugshot.sample.designsystem.component.MugshotTextButton
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotArtwork
import uk.co.fractalmotion.mugshot.sample.designsystem.foundation.MugshotAvatar
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.MugshotTheme
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

@Composable
internal fun ProfileScreen(state: ProfileUiState, modifier: Modifier = Modifier, scrollable: Boolean = true) {
  MugshotScreenScaffold(
    title = stringResource(R.string.profile_title),
    modifier = modifier,
    scrollable = scrollable,
    actions = { MugshotTextButton(text = stringResource(R.string.action_edit), onClick = {}) }
  ) {
    MugshotHeroCard {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large)
      ) {
        MugshotAvatar(
          initials = stringResource(state.initialsRes),
          size = 68.dp,
          paletteIndex = 2
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(state.nameRes),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
          )
          Text(
            text = stringResource(state.taglineRes),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f)
          )
        }
      }
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
      Text(
        text = pluralStringResource(
          R.plurals.profile_workouts_this_month,
          state.workoutsThisMonth,
          state.workoutsThisMonth
        ),
        style = MaterialTheme.typography.titleMedium,
        color = Color.White
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)) {
      ProfileStat(
        value = state.followers,
        label = stringResource(R.string.profile_stat_followers),
        modifier = Modifier.weight(1f)
      )
      ProfileStat(
        value = state.following,
        label = stringResource(R.string.profile_stat_following),
        modifier = Modifier.weight(1f)
      )
      ProfileStat(
        value = state.streak,
        label = stringResource(R.string.profile_stat_streak),
        modifier = Modifier.weight(1f)
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
      MugshotSectionHeader(title = stringResource(R.string.profile_section_achievements))
      state.achievements.forEach { achievement ->
        MugshotListRow(
          title = stringResource(achievement.titleRes),
          subtitle = stringResource(achievement.detailRes),
          leading = {
            MugshotArtwork(
              icon = achievement.icon,
              contentDescription = null,
              paletteIndex = achievement.paletteIndex,
              glyphSize = 22.dp,
              modifier = Modifier.size(46.dp)
            )
          }
        )
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
      MugshotSectionHeader(title = stringResource(R.string.profile_section_settings))
      state.settings.forEach { setting ->
        MugshotListRow(
          title = stringResource(setting.titleRes),
          subtitle = stringResource(setting.detailRes),
          trailing = { Switch(checked = setting.enabled, onCheckedChange = {}) }
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // theme_mode_caption is the sample's only -night resource override, so this chip proves the
      // night qualifier resolved as well as the Compose colour scheme flipping.
      MugshotStatusChip(text = stringResource(R.string.theme_mode_caption))
      Text(
        // generated_string_name comes from resValue(...) in build.gradle, so this caption is the
        // sample's only coverage of an AGP generated resource resolving inside layoutlib.
        text = stringResource(
          R.string.profile_build_caption,
          stringResource(R.string.generated_string_name)
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier = Modifier) {
  MugshotCard(modifier = modifier, contentPadding = MaterialTheme.spacing.medium) {
    Text(
      text = value,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Mugshot
@Preview
@Composable
internal fun ProfileScreenPreview() {
  MugshotTheme { ProfileScreen(state = ProfileFixtures.sample) }
}
