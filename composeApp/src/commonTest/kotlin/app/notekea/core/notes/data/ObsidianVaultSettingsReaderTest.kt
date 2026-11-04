package app.notekea.core.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileMetadata
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Which failures of a vault's `.obsidian` files fall back to Notekea's defaults, and which are bugs. */
class ObsidianVaultSettingsReaderTest {
    private val testDispatcher = StandardTestDispatcher()
    private val disk = FakeFileSystem()

    @Test
    fun `a settings file that is not JSON falls back to the default`() = runTest(context = testDispatcher) {
        disk.createDirectories(dir = FOLDER)
        disk.write(file = FOLDER / "app.json") { writeUtf8(string = "{ not json") }

        assertEquals(expected = null, actual = reader(disk).attachmentFolderPath(vaultPath = VAULT))
    }

    @Test
    fun `a disk that fails falls back to the default`() = runTest(context = testDispatcher) {
        val failing = failing(exception = IOException("disk"))

        assertEquals(expected = null, actual = reader(failing).attachmentFolderPath(vaultPath = VAULT))
    }

    @Test
    fun `a bug is not taken for an unreadable file`() = runTest(context = testDispatcher) {
        val buggy = failing(exception = IllegalStateException("a bug"))

        assertFailsWith<IllegalStateException> { reader(buggy).attachmentFolderPath(vaultPath = VAULT) }
    }

    private fun reader(
        fileSystem: FileSystem
    ) = ObsidianVaultSettingsReader(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    private fun failing(
        exception: Exception
    ): FileSystem = object : ForwardingFileSystem(delegate = disk) {
        override fun metadataOrNull(path: Path): FileMetadata? = throw exception
    }

    private companion object {
        const val VAULT = "/vault"
        val FOLDER = "/vault/.obsidian".toPath()
    }
}
