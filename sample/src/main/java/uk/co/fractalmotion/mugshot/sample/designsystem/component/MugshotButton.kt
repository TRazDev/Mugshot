package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** The single call to action on a screen. */
@Composable
internal fun MugshotPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = MaterialTheme.shapes.small,
    contentPadding = ButtonDefaults.ContentPadding
  ) {
    ButtonContent(text = text, icon = icon)
  }
}

/** A secondary action that still needs to carry weight. */
@Composable
internal fun MugshotTonalButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null
) {
  FilledTonalButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = MaterialTheme.shapes.small
  ) {
    ButtonContent(text = text, icon = icon)
  }
}

/** A low emphasis action that still needs an edge to sit against a busy surface. */
@Composable
internal fun MugshotOutlinedButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = MaterialTheme.shapes.small
  ) {
    ButtonContent(text = text, icon = icon)
  }
}

/** The lowest emphasis action. */
@Composable
internal fun MugshotTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
    Text(text = text, style = MaterialTheme.typography.labelLarge)
  }
}

@Composable
private fun RowScope.ButtonContent(text: String, icon: ImageVector?) {
  if (icon != null) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(modifier = Modifier.width(8.dp))
  }
  Text(text = text, style = MaterialTheme.typography.labelLarge)
}
