/*
 * Copyright (C) 2019 Square, Inc.
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
package uk.co.fractalmotion.mugshot.gradle

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import uk.co.fractalmotion.mugshot.gradle.utils.relativize

@CacheableTask
public abstract class PrepareResourcesTask : DefaultTask() {
  @get:Input
  public abstract val packageName: Property<String>

  @get:Input
  public abstract val targetSdkVersion: Property<String>

  @get:Input
  public abstract val projectResourceDirs: ListProperty<String>

  @get:Input
  public abstract val moduleResourceDirs: ListProperty<String>

  @get:Input
  public abstract val aarExplodedDirs: ListProperty<String>

  /**
   * The asset directories written to the resources file: the project's own, then its modules'.
   *
   * Files rather than paths like the properties around it, because a plugin can generate one --
   * Compose Multiplatform copies its resources into assets -- and Gradle will not read a generated
   * directory's path until the task producing it has run, while the configuration cache stores
   * inputs before anything runs. `@Internal`, because this task writes paths, not contents: an
   * asset changing must not invalidate it. Relativized when the task runs.
   */
  @get:Internal
  public abstract val projectAssetDirs: ConfigurableFileCollection

  /**
   * The paths of [projectAssetDirs] that no task generates, which key this task in its place.
   *
   * A generated directory's path is fixed by the task producing it, so it only changes when that
   * task is added or removed -- and that alone leaves the previous resources file in use.
   */
  @get:Input
  public abstract val staticProjectAssetDirs: ListProperty<String>

  @get:Input
  public abstract val aarAssetDirs: ListProperty<String>

  /** What [projectAssetDirs] are relativized against; not an input, as the paths already are. */
  @get:Internal
  public abstract val projectDirectory: DirectoryProperty

  @get:Input
  public abstract val nonTransitiveRClassEnabled: Property<Boolean>

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.NONE)
  public abstract val artifactFiles: ConfigurableFileCollection

  @get:OutputFile
  public abstract val mugshotResources: RegularFileProperty

  @TaskAction
  public fun writeResourcesFile() {
    val out = mugshotResources.get().asFile
    out.delete()

    val mainPackage = packageName.get()
    val resourcePackageNames = if (nonTransitiveRClassEnabled.get()) {
      buildList {
        add(mainPackage)
        artifactFiles.files.forEach { file ->
          // `first()` on an empty file throws NoSuchElementException, which says nothing about
          // which of a project's dependencies produced it.
          val packageName = file.useLines { lines -> lines.firstOrNull() }
          checkNotNull(packageName) {
            "Expected a package name in $file, which is empty. This file comes from an Android " +
              "dependency; the dependency is likely broken or was packaged incorrectly."
          }
          add(packageName)
        }
      }
    } else {
      listOf(mainPackage)
    }

    val config = Config(
      mainPackage = mainPackage,
      targetSdkVersion = targetSdkVersion.get(),
      resourcePackageNames = resourcePackageNames,
      projectResourceDirs = projectResourceDirs.get(),
      moduleResourceDirs = moduleResourceDirs.get(),
      aarExplodedDirs = aarExplodedDirs.get(),
      projectAssetDirs = projectAssetDirs.files.map { projectDirectory.get().relativize(it) },
      aarAssetDirs = aarAssetDirs.get()
    )
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()!!
    val json = moshi.adapter(Config::class.java).indent("  ").toJson(config)
    out.writeText(json)
  }

  internal data class Config(
    val mainPackage: String,
    val targetSdkVersion: String,
    val resourcePackageNames: List<String>,
    val projectResourceDirs: List<String>,
    val moduleResourceDirs: List<String>,
    val aarExplodedDirs: List<String>,
    val projectAssetDirs: List<String>,
    val aarAssetDirs: List<String>
  )
}
