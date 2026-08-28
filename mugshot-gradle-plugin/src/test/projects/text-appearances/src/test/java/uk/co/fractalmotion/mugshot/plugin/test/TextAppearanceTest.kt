/*
 * Copyright (C) 2020 Square, Inc.
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

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot

class TextAppearanceTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun inCode() {
    val context = mugshot.context
    val view = LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
      gravity = Gravity.CENTER
      setBackgroundResource(android.R.color.white)

      val textStyle = R.style.TextAppearance_Title
      addView(
        createTextView(context).apply {
          text = "Hello, Text Appearance!"
          setTextAppearance(textStyle)
        }
      )
      addView(
        createTextView(ContextThemeWrapper(context, textStyle)).apply {
          text = "Hello, Style!"
        }
      )
    }
    mugshot.snapshot(view)
  }

  @Test
  fun inXml() {
    val view = mugshot.inflate<LinearLayout>(R.layout.text_appearance_test)
    mugshot.snapshot(view)
  }

  private fun createTextView(context: Context) =
    TextView(context, null, 0).apply {
      layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
}
