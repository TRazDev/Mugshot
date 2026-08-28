package uk.co.fractalmotion.mugshot.plugin.test

import android.graphics.Color
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.lifecycle.Lifecycle
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import uk.co.fractalmotion.mugshot.Mugshot
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class LifecycleUsageTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test fun lifecycleOwner() {
    val view = View(mugshot.context).apply {
      setBackgroundColor(Color.BLUE)
    }
    var currentLifecycleState: Lifecycle.State? = null

    view.doOnAttach {
      val lifecycleOwner = view.findViewTreeSavedStateRegistryOwner()!!
      currentLifecycleState = lifecycleOwner.lifecycle.currentState
    }

    mugshot.snapshot(view)
    assertThat(currentLifecycleState).isNotNull()
    assertThat(currentLifecycleState).isEqualTo(Lifecycle.State.RESUMED)
  }

  @Test fun savedStateRegistryOwner() {
    val view = View(mugshot.context).apply {
      setBackgroundColor(Color.RED)
    }
    var savedStateRegistry: SavedStateRegistry? = null

    view.doOnAttach {
      val registryOwner = view.findViewTreeSavedStateRegistryOwner()!!
      savedStateRegistry = registryOwner.savedStateRegistry
    }

    mugshot.snapshot(view)
    assertThat(savedStateRegistry).isNotNull()
  }

  @Test fun onBackPressedDispatcherOwner() {
    val view = View(mugshot.context).apply {
      setBackgroundColor(Color.YELLOW)
    }
    var dispatcher: OnBackPressedDispatcher? = null

    view.doOnAttach {
      val dispatcherOwner = view.findViewTreeOnBackPressedDispatcherOwner()!!
      dispatcher = dispatcherOwner.onBackPressedDispatcher

      dispatcher.addCallback(
        object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() = Unit
        }
      )
    }

    mugshot.snapshot(view)
    assertThat(dispatcher).isNotNull()
  }

  private fun View.doOnAttach(action: (view: View) -> Unit) {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(view: View) = action(view)
      override fun onViewDetachedFromWindow(view: View) = Unit
    })
  }
}
