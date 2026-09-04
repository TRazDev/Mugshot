package uk.co.fractalmotion.mugshot.internal.resources

import com.android.ide.common.rendering.api.ResourceNamespace
import com.android.ide.common.resources.configuration.FolderConfiguration
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import uk.co.fractalmotion.mugshot.internal.resources.base.BasicResourceItem
import java.nio.file.Paths

/**
 * `FolderInfo.create` documents that it returns null for a folder name that is not a valid
 * resource folder. A qualifier the configuration parser rejects is one such name.
 */
internal class FolderInfoTest : RepositoryLoader<LoadableResourceRepository>(
  Paths.get("."),
  null,
  ResourceNamespace.RES_AUTO
) {
  override fun addResourceItem(item: BasicResourceItem, repository: LoadableResourceRepository) =
    throw UnsupportedOperationException("This loader exists only to reach FolderInfo.create.")

  @Test
  fun `the parser rejects a malformed qualifier`() {
    // Establishes the premise the rest of the test depends on.
    assertThat(FolderConfiguration.getConfigForQualifierString("!!!not-a-qualifier")).isNull()
  }

  @Test
  fun `a folder with a malformed qualifier is skipped`() {
    val cache = hashMapOf<String, FolderConfiguration>()

    val info = FolderInfo.create("values-!!!not-a-qualifier", cache)

    assertThat(info).isNull()
  }

  @Test
  fun `a folder with a valid qualifier is described`() {
    val cache = hashMapOf<String, FolderConfiguration>()

    val info = FolderInfo.create("values-en-rUS", cache)

    assertThat(info).isNotNull()
  }
}
