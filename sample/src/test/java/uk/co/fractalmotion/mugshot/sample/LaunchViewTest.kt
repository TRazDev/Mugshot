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

import android.widget.LinearLayout
import com.android.resources.ScreenOrientation.LANDSCAPE
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.DeviceConfig.Companion.NEXUS_5
import uk.co.fractalmotion.mugshot.DeviceConfig.Companion.PIXEL_3
import uk.co.fractalmotion.mugshot.Mugshot

class LaunchViewTest {
  @get:Rule
  val mugshot = Mugshot(deviceConfig = PIXEL_3)

  @Test
  fun pixel3() {
    val launch = mugshot.inflate<LinearLayout>(R.layout.launch)
    mugshot.snapshot(launch)
  }

  @Test
  fun pixel3_differentThemes() {
    mugshot.unsafeUpdateConfig(theme = "android:Theme.Material.Light")
    var launch = mugshot.inflate<LinearLayout>(R.layout.launch)
    mugshot.snapshot(view = launch, name = "light")

    mugshot.unsafeUpdateConfig(theme = "android:Theme.Material.Light.NoActionBar")
    launch = mugshot.inflate(R.layout.launch)
    mugshot.snapshot(view = launch, name = "light no_action_bar")
  }

  @Test
  fun nexus5_differentOrientations() {
    mugshot.unsafeUpdateConfig(deviceConfig = NEXUS_5)
    var launch = mugshot.inflate<LinearLayout>(R.layout.launch)
    mugshot.snapshot(launch, "portrait")

    mugshot.unsafeUpdateConfig(deviceConfig = NEXUS_5.copy(orientation = LANDSCAPE))
    launch = mugshot.inflate(R.layout.launch)
    mugshot.snapshot(launch, "landscape")
  }
}
