package uk.co.fractalmotion.mugshot.sample.feature.health

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class HealthUiState(
  val rings: List<ActivityRing>,
  val metrics: List<HealthMetric>,
  val sleepStages: List<SleepStage>,
  val sleepHours: Int,
  val sleepMinutes: Int,
  val restingHeartRateTrend: List<Float>,
  val hasData: Boolean = true
)

internal data class ActivityRing(
  @StringRes val labelRes: Int,
  val progress: Float,
  val value: String
)

internal data class HealthMetric(
  @StringRes val labelRes: Int,
  @StringRes val supportingRes: Int,
  @DrawableRes val icon: Int,
  val value: String,
  val supportingArg: Int? = null
)

internal data class SleepStage(
  @StringRes val labelRes: Int,
  val fraction: Float
)
