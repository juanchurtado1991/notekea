package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class OkioNoteContentStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeFileSystem = FakeFileSystem().apply { createDirectories(dir = VAULT.toPath() / FOLDER) }

    @Test
    fun `text round-trips exactly, byte order mark and windows line endings included`() = runStoreTest {
        val text = "\uFEFF# Arepas\r\n\r\nSee [[Café con leche]].\r\n"
        writeNote(content = ORIGINAL)

        val written = store().write(vaultPath = VAULT, path = NOTE, content = text)

        assertEquals(
            expected = Outcome.Success(value = Unit) to text,
            actual = written to store().read(vaultPath = VAULT, path = NOTE)
        )
        assertEquals(
            expected = listOf("Arepas.md"),
            actual = fakeFileSystem.list(dir = VAULT.toPath() / FOLDER).map { it.name }
        )
    }

    @Test
    fun `a failed write leaves the note as it was and no temporary file behind`() = runStoreTest {
        writeNote(content = ORIGINAL)

        val outcome = store(fileSystem = refusingMoves()).write(vaultPath = VAULT, path = NOTE, content = "changed")

        assertEquals(
            expected = Outcome.Failure(error = NotesError.OperationFailed(path = NOTE.value)) to ORIGINAL,
            actual = outcome to store().read(vaultPath = VAULT, path = NOTE)
        )
        assertEquals(
            expected = listOf("Arepas.md"),
            actual = fakeFileSystem.list(dir = VAULT.toPath() / FOLDER).map { it.name }
        )
    }

    @Test
    fun `a move refused for a moment, as Windows does while the note is being read, is tried again`() = runStoreTest {
        writeNote(content = ORIGINAL)

        val outcome = store(fileSystem = refusingMoves(times = 3)).write(vaultPath = VAULT, path = NOTE, content = TEXT)

        assertEquals(
            expected = Outcome.Success(value = Unit) to TEXT,
            actual = outcome to store().read(vaultPath = VAULT, path = NOTE)
        )
        assertEquals(
            expected = listOf("Arepas.md"),
            actual = fakeFileSystem.list(dir = VAULT.toPath() / FOLDER).map { it.name }
        )
    }

    @Test
    fun `a note that cannot be read reads as null`() = runStoreTest {
        assertEquals(
            expected = null,
            actual = store().read(vaultPath = VAULT, path = RelativePath(value = "Gone.md"))
        )
    }

    /** A file system that refuses the first [times] moves, every move unless told. */
    private fun refusingMoves(
        times: Int = Int.MAX_VALUE
    ): FileSystem = object : ForwardingFileSystem(delegate = fakeFileSystem) {
        private var refused = 0

        override fun atomicMove(
            source: Path,
            target: Path
        ) {
            if (refused < times) {
                refused++
                throw IOException(DENIED)
            }
            super.atomicMove(source = source, target = target)
        }
    }

    private fun runStoreTest(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { block() }

    private fun store(
        fileSystem: FileSystem = fakeFileSystem
    ): OkioNoteContentStore = OkioNoteContentStore(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    private fun writeNote(
        content: String
    ) {
        fakeFileSystem.write(file = VAULT.toPath() / NOTE.value) { writeUtf8(string = content) }
    }

    companion object {
        private const val DENIED = "File in use"
        private const val FOLDER = "Recipes"
        private const val ORIGINAL = "# Arepas\n"
        private const val TEXT = "# Arepas\n\nServe them warm.\n"
        private const val VAULT = "/home/ana/Notes"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}
