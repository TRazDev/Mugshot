package uk.co.fractalmotion.mugshot.sample.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.fractalmotion.mugshot.sample.designsystem.theme.spacing

/**
 * The frame every feature screen sits in.
 *
 * Fills the viewport deliberately: a screen that only wraps its content lets the framework window
 * background show through around the edges, which reads as a rendering bug in a golden.
 *
 * [scrollable] picks which layer does the scrolling, and the two are genuinely exclusive:
 *
 *  - `true` (the default) puts the content in a `verticalScroll` inside a `Scaffold`. A screen
 *    taller than the device is clipped, which is what `RenderingMode.NORMAL` snapshots.
 *  - `false` lays the bar and content out directly so the whole screen can be measured at its full
 *    height, which is what `RenderingMode.V_SCROLL` needs. `Scaffold` cannot be used here at all —
 *    it lays out at the incoming maximum height, and V_SCROLL leaves that unbounded — and
 *    `Modifier.verticalScroll` throws outright when measured with an infinite maximum.
 */
@Composable
internal fun MugshotScreenScaffold(
  title: String,
  modifier: Modifier = Modifier,
  onBack: (() -> Unit)? = null,
  actions: (@Composable () -> Unit)? = null,
  scrollable: Boolean = true,
  content: @Composable ColumnScope.() -> Unit
) {
  if (scrollable) {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.background,
      topBar = { MugshotTopBar(title = title, onBack = onBack, actions = actions) }
    ) { insets ->
      ScreenBody(
        modifier = Modifier
          .fillMaxSize()
          .padding(insets)
          .verticalScroll(rememberScrollState()),
        content = content
      )
    }
  } else {
    Surface(
      modifier = modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        MugshotTopBar(title = title, onBack = onBack, actions = actions)
        ScreenBody(modifier = Modifier.fillMaxWidth(), content = content)
      }
    }
  }
}

@Composable
private fun ScreenBody(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
  // Content is centred and capped rather than stretched edge to edge. Without this the tablet and
  // fold goldens are just the phone layout pulled wide, with cards several hundred dp across.
  Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
    Column(
      modifier = Modifier
        .widthIn(max = MaxContentWidth)
        .padding(horizontal = MaterialTheme.spacing.gutter),
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraLarge)
    ) {
      content()
      Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
  }
}

private val MaxContentWidth = 560.dp
