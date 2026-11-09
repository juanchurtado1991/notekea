package app.notekea.core.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class OkioAttachmentStoreTest {
    private val store = OkioAttachmentStore(
        fileSystem = FakeFileSystem(),
        dispatchers = FakeAppDispatchers(dispatcher = StandardTestDispatcher())
    )

    @Test
    fun `a file is inside the vault only under its folder, not in one whose name starts the same`() {
        assertEquals(
            expected = listOf(RelativePath(value = "pics/pic.png"), null, null, null),
            actual = listOf(
                "/home/me/notes/pics/pic.png",
                "/home/me/notes-old/pic.png",
                "/home/me/notes",
                "/home/me/notes/../pic.png"
            ).map {
                store.pathInVault(
                    vaultPath = "/home/me/notes",
                    absolutePath = it
                )
            }
        )
    }
}
