package uk.co.fractalmotion.mugshot.sample.screen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import uk.co.fractalmotion.mugshot.RenderExtension

/**
 * Draws a baseline grid over whatever is being snapshotted.
 *
 * A [RenderExtension] gets the content `View` after composition and returns the view that is
 * actually rendered, so it can wrap, decorate or replace the tree. That makes it the hook for
 * anything you want in the golden but not in the app — a grid, a watermark, a locale banner.
 *
 * It works on Views even for Compose content, because a composable is hosted in a `ComposeView` by
 * the time the extension sees it.
 */
class GridOverlayRenderExtension(private val stepDp: Int = 8) : RenderExtension {
  override fun renderView(contentView: View): View {
    val context = contentView.context
    return FrameLayout(context).apply {
      addView(contentView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
      addView(GridView(context, stepDp), FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }
  }
}

private class GridView(context: Context, private val stepDp: Int) : View(context) {
  private val paint = Paint().apply {
    color = Color.argb(38, 255, 0, 128)
    strokeWidth = 1f
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    val step = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      stepDp.toFloat(),
      resources.displayMetrics
    )
    if (step <= 0f) return
    var x = step
    while (x < width) {
      canvas.drawLine(x, 0f, x, height.toFloat(), paint)
      x += step
    }
    var y = step
    while (y < height) {
      canvas.drawLine(0f, y, width.toFloat(), y, paint)
      y += step
    }
  }
}
