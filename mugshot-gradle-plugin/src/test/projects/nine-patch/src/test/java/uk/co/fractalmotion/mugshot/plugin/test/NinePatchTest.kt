package uk.co.fractalmotion.mugshot.plugin.test

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import uk.co.fractalmotion.mugshot.Mugshot
import org.junit.Rule
import org.junit.Test

class NinePatchTest {
  @get:Rule
  val mugshot = Mugshot(theme = "Theme.App")

  @Test
  fun ninePatch() {
    val launch = LinearLayout(mugshot.context).apply {
      val outValue = TypedValue()
      context.theme.resolveAttribute(android.R.attr.listDivider, outValue, true)
      dividerDrawable = AppCompatResources.getDrawable(context, outValue.resourceId)

      finishSetup()
    }
    mugshot.snapshot(launch)
  }

  private fun LinearLayout.finishSetup() {
    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
    orientation = VERTICAL
    gravity = CENTER
    setBackgroundColor(Color.GRAY)
    addView(
      TextView(context).apply {
        text = "Hello"
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32f)
      },
      WRAP_CONTENT,
      WRAP_CONTENT
    )
    addView(
      TextView(context).apply {
        text = "Mugshot"
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32f)
      },
      WRAP_CONTENT,
      WRAP_CONTENT
    )
  }
}
