package uk.co.fractalmotion.mugshot.sample.feature.profile

import uk.co.fractalmotion.mugshot.sample.R

/** Sample content for the profile screen, shared by its preview and every test that renders it. */
internal object ProfileFixtures {
  val sample = ProfileUiState(
    nameRes = R.string.profile_name,
    initialsRes = R.string.profile_initials,
    taglineRes = R.string.profile_tagline,
    followers = "2,481",
    following = "312",
    streak = "42",
    workoutsThisMonth = 12,
    achievements = listOf(
      Achievement(
        icon = R.drawable.ic_flame,
        titleRes = R.string.profile_achievement_early_bird,
        detailRes = R.string.profile_achievement_early_bird_detail,
        paletteIndex = 0
      ),
      Achievement(
        icon = R.drawable.ic_footsteps,
        titleRes = R.string.profile_achievement_century,
        detailRes = R.string.profile_achievement_century_detail,
        paletteIndex = 1
      ),
      Achievement(
        icon = R.drawable.ic_heart_pulse,
        titleRes = R.string.profile_achievement_streak,
        detailRes = R.string.profile_achievement_streak_detail,
        paletteIndex = 2
      )
    ),
    settings = listOf(
      ProfileSetting(
        titleRes = R.string.profile_setting_notifications,
        detailRes = R.string.profile_setting_notifications_detail,
        enabled = true
      ),
      ProfileSetting(
        titleRes = R.string.profile_setting_privacy,
        detailRes = R.string.profile_setting_privacy_detail,
        enabled = false
      ),
      ProfileSetting(
        titleRes = R.string.profile_setting_units,
        detailRes = R.string.profile_setting_units_detail,
        enabled = true
      )
    )
  )
}
