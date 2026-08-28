package uk.co.fractalmotion.mugshot.preview.runtime

import androidx.compose.runtime.Composable

public data class MugshotPreviewData(
  val snapshotName: String,
  val composable: @Composable () -> Unit
)
