/*
 * Copyright (C) 2019 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.fractalmotion.mugshot

import android.animation.AnimationHandler
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import com.android.internal.lang.System_Delegate
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class MugshotTest {
  @get:Rule
  val testRule = MugshotTestRule()

  val mugshot
    get() = testRule.mugshot

  @Test
  fun drawCalls() {
    val log = mutableListOf<String>()

    val view = object : View(mugshot.context) {
      override fun onDraw(canvas: Canvas) {
        log += "onDraw time=$time"
      }
    }

    mugshot.snapshot(view)

    assertThat(log).containsExactly("onDraw time=0", "onDraw time=0")
  }

  @Test
  fun resetsAnimationHandler() {
    assertThat(AnimationHandler.sAnimatorHandler.get()).isNull()

    // Why Button?  Because it sets a StateListAnimator on window attach
    // See https://github.com/cashapp/paparazzi/pull/319
    mugshot.snapshot(Button(mugshot.context))

    assertThat(AnimationHandler.sAnimatorHandler.get()).isNull()
  }

  @Test
  fun animationCallbacksForStaticSnapshots() {
    val log = mutableListOf<String>()

    val view = object : TextView(mugshot.context) {
      override fun onDraw(canvas: Canvas) {
        log += "onDraw text=$text"
      }
    }

    val animator = ValueAnimator.ofInt(200, 300)
    animator.addUpdateListener {
      view.text = it.animatedFraction.toString()
    }
    animator.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationStart(animation: Animator, isReverse: Boolean) {
        log += "onAnimationStart uptimeMillis=$uptime"
      }

      override fun onAnimationEnd(animator: Animator) {
        log += "onAnimationEnd uptimeMillis=$uptime"
      }
    })

    animator.startDelay = 2_000L
    animator.duration = 1_000L
    animator.interpolator = LinearInterpolator()
    assertThat(AnimationHandler.getAnimationCount()).isEqualTo(0)
    animator.start()
    assertThat(AnimationHandler.getAnimationCount()).isEqualTo(1)

    mugshot.snapshot(view, offsetMillis = 0L)
    assertThat(log).containsExactly(
      "onDraw text=",
      "onDraw text="
    )
    log.clear()

    mugshot.snapshot(view, offsetMillis = 2_000L)
    assertThat(log).containsExactly(
      "onAnimationStart uptimeMillis=2000",
      "onDraw text=0.0"
    )
    log.clear()

    mugshot.snapshot(view, offsetMillis = 2_500L)
    assertThat(log).containsExactly(
      "onDraw text=0.5"
    )
    log.clear()

    mugshot.snapshot(view, offsetMillis = 3_000L)
    assertThat(log).containsExactly(
      "onAnimationEnd uptimeMillis=3000",
      "onDraw text=1.0"
    )
    assertThat(AnimationHandler.getAnimationCount()).isEqualTo(0)
    log.clear()
  }

  @Test
  fun throwsRenderingExceptions() {
    val view = object : View(mugshot.context) {
      override fun onAttachedToWindow() {
        throw Throwable("Oops")
      }
    }

    val thrown = try {
      mugshot.snapshot(view)
      false
    } catch (exception: Throwable) {
      true
    }

    assertThat(thrown).isTrue()
  }

  private val time: Long
    get() {
      return TimeUnit.NANOSECONDS.toMillis(System_Delegate.nanoTime())
    }

  private val uptime: Long
    get() {
      return SystemClock.uptimeMillis()
    }
}
