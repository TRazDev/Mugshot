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
  @param:StringRes val labelRes: Int,
  val progress: Float,
  val value: String
)

internal data class HealthMetric(
  @param:StringRes val labelRes: Int,
  @param:StringRes val supportingRes: Int,
  @param:DrawableRes val icon: Int,
  val value: String,
  val supportingArg: Int? = null
)

internal data class SleepStage(
  @param:StringRes val labelRes: Int,
  val fraction: Float
)
