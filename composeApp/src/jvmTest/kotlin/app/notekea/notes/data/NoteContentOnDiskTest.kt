package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Notes on real files: what the disk does and the in-memory file system does not. */
@OptIn(ExperimentalPathApi::class)
class NoteContentOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val vault = Files.createTempDirectory(TEMP_PREFIX)
    private val vaultPath = vault.toString()
    private val store = OkioNoteContentStore(
        fileSystem = FileSystem.SYSTEM,
        dispatchers = dispatchers,
        attributes = NioNoteFileAttributes()
    )

    @AfterTest
    fun deleteVault() {
        vault.deleteRecursively()
    }

    @Test
    fun `a note without read permission is there but unreadable, not gone`() = runTest(context = testDispatcher) {
        // Windows has no POSIX permissions to take away.
        if ("posix" !in FileSystems.getDefault().supportedFileAttributeViews()) return@runTest
        val file = vault.resolve(NOTE.value).also { Files.writeString(it, TEXT) }
        Files.setPosixFilePermissions(file, emptySet())

        assertEquals(
            expected = Outcome.Failure(error = NotesError.Unreadable(path = NOTE.value)),
            actual = store.readText(vaultPath = vaultPath, path = NOTE)
        )
    }

    @Test
    fun `a save keeps the permissions of the note it replaces, and a new note is written as usual`() =
        runTest(context = testDispatcher) {
            if ("posix" !in FileSystems.getDefault().supportedFileAttributeViews()) return@runTest
            val file = vault.resolve(NOTE.value).also { Files.writeString(it, TEXT) }
            val permissions = PosixFilePermissions.fromString("rw-r-----")
            Files.setPosixFilePermissions(file, permissions)
            val created = RelativePath(value = "New.md")

            store.write(vaultPath = vaultPath, path = NOTE, content = EDITED)
            store.write(vaultPath = vaultPath, path = created, content = TEXT)

            assertEquals(expected = permissions, actual = Files.getPosixFilePermissions(file))
            assertEquals(expected = EDITED, actual = Files.readString(file))
            assertEquals(expected = TEXT, actual = Files.readString(vault.resolve(created.value)))
        }

    companion object {
        private const val EDITED = "# Arepas\n\nEdited.\n"
        private const val TEMP_PREFIX = "notekea-note-content"
        private const val TEXT = "# Arepas\n"
        private val NOTE = RelativePath(value = "Arepas.md")
    }
}
