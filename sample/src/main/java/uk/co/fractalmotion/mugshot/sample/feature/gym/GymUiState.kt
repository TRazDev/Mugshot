package uk.co.fractalmotion.mugshot.sample.feature.gym

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class GymUiState(
  @StringRes val planNameRes: Int,
  @StringRes val streakHeadlineRes: Int,
  @StringRes val streakDetailRes: Int,
  val volumeByWeek: List<Float>,
  val volumeWeekLabels: List<String>,
  val sessions: List<WorkoutSummary>
)

internal data class WorkoutSummary(
  @StringRes val nameRes: Int,
  @StringRes val detailRes: Int,
  @DrawableRes val icon: Int,
  val paletteIndex: Int
)

internal data class WorkoutDetailUiState(
  @StringRes val nameRes: Int,
  val volume: String,
  val duration: String,
  val workingSets: String,
  val exercises: List<Exercise>
)

internal data class Exercise(
  @StringRes val nameRes: Int,
  val sets: Int,
  val reps: Int,
  val weight: String
)
