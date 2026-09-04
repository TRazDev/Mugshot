package uk.co.fractalmotion.mugshot.sample.feature.gym

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class GymUiState(
  @param:StringRes val planNameRes: Int,
  @param:StringRes val streakHeadlineRes: Int,
  @param:StringRes val streakDetailRes: Int,
  val volumeByWeek: List<Float>,
  val volumeWeekLabels: List<String>,
  val sessions: List<WorkoutSummary>
)

internal data class WorkoutSummary(
  @param:StringRes val nameRes: Int,
  @param:StringRes val detailRes: Int,
  @param:DrawableRes val icon: Int,
  val paletteIndex: Int
)

internal data class WorkoutDetailUiState(
  @param:StringRes val nameRes: Int,
  val volume: String,
  val duration: String,
  val workingSets: String,
  val exercises: List<Exercise>
)

internal data class Exercise(
  @param:StringRes val nameRes: Int,
  val sets: Int,
  val reps: Int,
  val weight: String
)
