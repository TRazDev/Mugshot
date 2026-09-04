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
  @param:StringRes val nameRes: Int,
  @param:StringRes val initialsRes: Int,
  @param:StringRes val taglineRes: Int,
  val followers: String,
  val following: String,
  val streak: String,
  val workoutsThisMonth: Int,
  val achievements: List<Achievement>,
  val settings: List<ProfileSetting>
)

internal data class Achievement(
  @param:DrawableRes val icon: Int,
  @param:StringRes val titleRes: Int,
  @param:StringRes val detailRes: Int,
  val paletteIndex: Int
)

internal data class ProfileSetting(
  @param:StringRes val titleRes: Int,
  @param:StringRes val detailRes: Int,
  val enabled: Boolean
)
