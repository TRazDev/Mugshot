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

import com.google.common.base.CharMatcher
import dev.drewhamilton.poko.Poko
import java.security.MessageDigest
import java.util.Date
import java.util.Locale

@Poko
public class Snapshot(
  public val name: String?,
  public val testName: TestName,
  public val timestamp: Date,
  public val tags: List<String> = listOf(),
  public val file: String? = null
) {
  public fun copy(
    name: String? = this.name,
    testName: TestName = this.testName,
    timestamp: Date = this.timestamp,
    tags: List<String> = this.tags,
    file: String? = this.file
  ): Snapshot = Snapshot(name, testName, timestamp, tags, file)
}

internal val invalidPrintableChars = CharMatcher.anyOf("<>:\"/\\|?*")

internal fun Snapshot.toFileName(delimiter: String = "_", extension: String): String {
  val formattedLabel = if (name != null) {
    if (invalidPrintableChars.matchesAnyOf(name)) {
      throw IllegalArgumentException("Supplied snapshot name contains invalid characters ('$name')")
    }
    "$delimiter${name.lowercase(Locale.US).replace("\\s".toRegex(), delimiter)}"
  } else {
    ""
  }
  if (invalidPrintableChars.matchesAnyOf(testName.methodName)) {
    throw IllegalArgumentException("Generated method name contains invalid characters ('${testName.methodName}')")
  }
  val name = buildString {
    append(testName.packageName)
    append(delimiter)
    append(testName.className)
    append(delimiter)
    append(testName.methodName.replace("\\s".toRegex(), delimiter))
    append(formattedLabel)
  }
  return "${name.shortenedIfLongerThanAFilesystemAllows(extension)}.$extension"
}

/**
 * The longest a golden's name may be, extension included.
 *
 * A name is the package, the class, the method and any label run together, and nothing about
 * those is bounded. Filesystems cap a single name at 255 characters, and Windows caps a whole
 * path at 260 unless long paths are turned on, so a deep module with long names can produce a
 * file that cannot be written at all. The limit sits well above what real tests produce, 136
 * characters for the longest in this repository's own sample, so recorded goldens keep the names
 * they have.
 */
private const val MAX_FILE_NAME_LENGTH = 200

/**
 * Replaces the tail of an over-long name with a hash of the whole of it.
 *
 * The hash is what keeps two long names apart once their readable beginnings are identical, and
 * it is derived from the full name so it is the same on every machine and every run: a golden
 * recorded on one and verified on another has to agree.
 */
private fun String.shortenedIfLongerThanAFilesystemAllows(extension: String): String {
  val budget = MAX_FILE_NAME_LENGTH - extension.length - 1
  if (length <= budget) return this

  val digest = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray())
    .joinToString("") { "%02x".format(it) }
    .take(HASH_LENGTH)
  return take(budget - HASH_LENGTH - 1) + "~" + digest
}

private const val HASH_LENGTH = 12
