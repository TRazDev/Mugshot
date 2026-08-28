package uk.co.fractalmotion.mugshot.plugin.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.ide.common.rendering.api.SessionParams
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class HashTest {
  @get:Rule
  val mugshot = Mugshot(
    renderingMode = SessionParams.RenderingMode.SHRINK
  )

  @Test
  fun verticalLineComponent() {
    mugshot.snapshot {
      Spacer(
        modifier = Modifier
          .width(1.dp)
          .height(4.dp)
          .background(Color.Red)
      )
    }
  }

  @Test
  fun horizontalLineComponent() {
    mugshot.snapshot {
      Spacer(
        modifier = Modifier
          .width(4.dp)
          .height(1.dp)
          .background(Color.Red)
      )
    }
  }

  @Test
  fun squareComponent() {
    mugshot.snapshot {
      Spacer(
        modifier = Modifier
          .width(2.dp)
          .height(2.dp)
          .background(Color.Red)
      )
    }
  }
}
