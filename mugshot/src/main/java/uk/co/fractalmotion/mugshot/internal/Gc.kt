/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.fractalmotion.mugshot.internal

import java.lang.ref.WeakReference

internal object Gc {
  /**
   * Forces a real collection by waiting for a canary object to be cleared.
   *
   * This used to call `System.runFinalization()` alongside each `System.gc()`. That is deprecated
   * because finalization is being removed from Java, so the call would stop compiling rather than
   * degrade, and once finalization is gone there is nothing for it to force.
   *
   * Dropping it was measured rather than assumed, since `android.graphics` classes such as
   * `Region`, `PathEffect`, `NinePatch` and `VectorDrawable` still free native memory in
   * `finalize()`. Over the sample's 44 renders, peak resident memory of the test worker was
   * 834/826/840 MB with the calls and 811/830/832 MB without: no difference beyond run to run
   * noise. The JVM's finalizer thread still runs finalizers regardless, so the calls only ever
   * forced promptness, not the finalization itself.
   */
  fun gc() {
    // See RuntimeUtil#gc in jlibs (http://jlibs.in/)
    var obj: Any? = Any()
    val ref = WeakReference<Any>(obj)

    @Suppress("UNUSED_VALUE") // The null is unused, but it's important that the obj local variable loses the reference.
    obj = null
    while (ref.get() != null) {
      System.gc()
    }

    System.gc()
  }
}
