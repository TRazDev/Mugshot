package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The sample's app bar. Kept flat — the screens below it carry the colour. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MugshotTopBar(
  title: String,
  modifier: Modifier = Modifier,
  onBack: (() -> Unit)? = null,
  actions: (@Composable () -> Unit)? = null
) {
  TopAppBar(
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
    },
    modifier = modifier,
    navigationIcon = {
      if (onBack != null) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    },
    actions = { actions?.invoke() },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background
    )
  )
}
