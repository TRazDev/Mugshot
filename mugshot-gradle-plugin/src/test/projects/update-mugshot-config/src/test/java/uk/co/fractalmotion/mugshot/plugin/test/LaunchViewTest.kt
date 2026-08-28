/*
 * Copyright (C) 2022 Block, Inc.
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

import android.widget.LinearLayout
import org.junit.Rule
import org.junit.Test
import uk.co.fractalmotion.mugshot.DeviceConfig.Companion.NEXUS_7
import uk.co.fractalmotion.mugshot.DeviceConfig.Companion.PIXEL_3
import uk.co.fractalmotion.mugshot.Mugshot

class LaunchViewTest {
  @get:Rule
  val mugshot = Mugshot(deviceConfig = PIXEL_3)

  @Test
  fun updatingConfigUpdatesResources() {
    var launch = mugshot.inflate<LinearLayout>(R.layout.launch)
    mugshot.snapshot(launch, "pixel3")
    mugshot.unsafeUpdateConfig(deviceConfig = NEXUS_7)
    launch = mugshot.inflate(R.layout.launch)
    mugshot.snapshot(launch, "nexus7")
  }
}
