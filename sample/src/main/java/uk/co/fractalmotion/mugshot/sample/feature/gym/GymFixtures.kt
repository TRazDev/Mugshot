package uk.co.fractalmotion.mugshot.sample.feature.gym

import uk.co.fractalmotion.mugshot.sample.R

internal object GymFixtures {
  val sample = GymUiState(
    planNameRes = R.string.gym_plan_name,
    streakHeadlineRes = R.string.gym_streak_headline,
    streakDetailRes = R.string.gym_streak_detail,
    volumeByWeek = listOf(12.4f, 14.1f, 11.8f, 16.2f, 15.0f, 17.6f),
    volumeWeekLabels = listOf("1", "2", "3", "4", "5", "6"),
    sessions = listOf(
      WorkoutSummary(
        nameRes = R.string.gym_workout_push,
        detailRes = R.string.gym_workout_push_detail,
        icon = R.drawable.ic_dumbbell,
        paletteIndex = 0
      ),
      WorkoutSummary(
        nameRes = R.string.gym_workout_pull,
        detailRes = R.string.gym_workout_pull_detail,
        icon = R.drawable.ic_dumbbell,
        paletteIndex = 1
      ),
      WorkoutSummary(
        nameRes = R.string.gym_workout_legs,
        detailRes = R.string.gym_workout_legs_detail,
        icon = R.drawable.ic_footsteps,
        paletteIndex = 2
      )
    )
  )

  val pushDay = WorkoutDetailUiState(
    nameRes = R.string.gym_detail_title,
    volume = "7,240 kg",
    duration = "52 min",
    workingSets = "24",
    exercises = listOf(
      Exercise(nameRes = R.string.gym_exercise_bench, sets = 4, reps = 8, weight = "72.5kg"),
      Exercise(nameRes = R.string.gym_exercise_incline, sets = 4, reps = 10, weight = "28kg"),
      Exercise(nameRes = R.string.gym_exercise_dips, sets = 3, reps = 12, weight = "+15kg"),
      Exercise(nameRes = R.string.gym_exercise_lateral, sets = 4, reps = 15, weight = "10kg"),
      Exercise(nameRes = R.string.gym_exercise_triceps, sets = 3, reps = 14, weight = "32kg")
    )
  )
}
