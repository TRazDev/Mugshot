package uk.co.fractalmotion.mugshot.plugin.test

import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.DeviceConfig
import uk.co.fractalmotion.mugshot.Mugshot

class RenderingModesTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun renderingModes() {
    val linearLayout = LinearLayout(mugshot.context).apply {
      layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }

    (0..2).forEach {
      linearLayout.addView(
        TextView(mugshot.context).apply {
          text = "$it"
          textSize = 128f
          gravity = Gravity.CENTER
          layoutParams = LayoutParams(DeviceConfig.NEXUS_5.screenWidth, DeviceConfig.NEXUS_5.screenHeight)
        }
      )
    }

    mugshot.snapshot(view = linearLayout, name = "normal") // defaults to NORMAL
    mugshot.unsafeUpdateConfig(renderingMode = RenderingMode.H_SCROLL)
    mugshot.snapshot(view = linearLayout, name = "horizontal_scroll")

    mugshot.unsafeUpdateConfig(renderingMode = RenderingMode.V_SCROLL)
    linearLayout.orientation = LinearLayout.VERTICAL
    mugshot.snapshot(view = linearLayout, name = "vertical_scroll")

    mugshot.unsafeUpdateConfig(renderingMode = RenderingMode.SHRINK)
    mugshot.snapshot(
      view = TextView(mugshot.context).apply {
        text = "0"
        textSize = 20f
        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
      },
      name = "shrink"
    )
  }
}
