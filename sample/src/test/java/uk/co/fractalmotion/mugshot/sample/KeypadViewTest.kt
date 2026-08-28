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
package uk.co.fractalmotion.mugshot.sample

import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.Mugshot
import uk.co.fractalmotion.mugshot.sample.databinding.KeypadBinding

class KeypadViewTest {
  @get:Rule
  val mugshot = Mugshot()

  @Test
  fun testViews() {
    val binding = KeypadBinding.inflate(mugshot.layoutInflater)

    with(binding) {
      amount.text = "$0"
      mugshot.snapshot(root, "zero dollars")

      amount.text = "$5.00"
      mugshot.snapshot(root, "five bucks")

      root.setBackgroundResource(R.color.keypadDarkGrey)
      val darkGrey = mugshot.context.getColor(R.color.keypadDarkGrey)
      root.setBackgroundColor(darkGrey)
      amount.text = "$1.00"
      mugshot.snapshot(root, "grey")

      root.setBackgroundResource(R.color.keypadDarkGrey)
      root.setBackgroundColor(mugshot.context.getColor(R.color.bolt))
      amount.setTextColor(darkGrey)
      amount123.setTextColor(darkGrey)
      amount456.setTextColor(darkGrey)
      amount789.setTextColor(darkGrey)
      amount0.setTextColor(darkGrey)
      amount.text = ".01 BTC"

      mugshot.snapshot(root, "bolt")
    }
  }
}
