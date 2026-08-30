package uk.co.fractalmotion.mugshot.sample.feature.profile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Profile screen state.
 *
 * Labels are carried as string resource ids rather than resolved text so the screen looks them up
 * under whatever locale the snapshot is rendering in. Holding resolved strings here would make the
 * locale and pseudolocale goldens identical to the default one.
 */
internal data class ProfileUiState(
  @StringRes val nameRes: Int,
  @StringRes val initialsRes: Int,
  @StringRes val taglineRes: Int,
  val followers: String,
  val following: String,
  val streak: String,
  val workoutsThisMonth: Int,
  val achievements: List<Achievement>,
  val settings: List<ProfileSetting>
)

internal data class Achievement(
  @DrawableRes val icon: Int,
  @StringRes val titleRes: Int,
  @StringRes val detailRes: Int,
  val paletteIndex: Int
)

internal data class ProfileSetting(
  @StringRes val titleRes: Int,
  @StringRes val detailRes: Int,
  val enabled: Boolean
)
