package uk.co.fractalmotion.mugshot.internal

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

internal class MugshotLifecycleOwner : LifecycleOwner {
  val registry = LifecycleRegistry(this)
  override val lifecycle: Lifecycle
    get() = registry
}

internal class MugshotSavedStateRegistryOwner(
  private val lifecycleOwner: LifecycleOwner
) : SavedStateRegistryOwner, LifecycleOwner by lifecycleOwner {
  private val controller = SavedStateRegistryController.create(this).apply { performRestore(null) }
  override val savedStateRegistry: SavedStateRegistry = controller.savedStateRegistry
}

internal class MugshotOnBackPressedDispatcherOwner(
  private val lifecycleOwner: LifecycleOwner
) : OnBackPressedDispatcherOwner, LifecycleOwner by lifecycleOwner {
  override val onBackPressedDispatcher: OnBackPressedDispatcher
    get() = OnBackPressedDispatcher { /* Swallow all back-presses. */ }
}
