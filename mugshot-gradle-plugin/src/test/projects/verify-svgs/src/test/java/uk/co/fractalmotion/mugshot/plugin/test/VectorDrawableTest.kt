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
package uk.co.fractalmotion.mugshot.plugin.test

import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout.LayoutParams
import androidx.appcompat.widget.AppCompatImageView
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class VectorDrawableTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun vectorDrawable() {
    val imageView = AppCompatImageView(mugshot.context).apply {
      layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        .apply {
          gravity = CENTER
          height = 400
          width = 400
        }
      setImageResource(R.drawable.arrow_up)
    }
    mugshot.snapshot(imageView, "arrow up")
  }
}
