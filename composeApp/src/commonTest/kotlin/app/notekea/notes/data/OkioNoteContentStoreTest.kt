package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FakeNoteFileAttributes
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileHandle
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
    private val attributes = FakeNoteFileAttributes()

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
    fun `a save reaches the disk before it replaces the note, so a power cut cannot lose both`() = runStoreTest {
        writeNote(content = ORIGINAL)
        val steps = mutableListOf<String>()
        val recording = object : ForwardingFileSystem(delegate = fakeFileSystem) {
            override fun openReadWrite(
                file: Path,
                mustCreate: Boolean,
                mustExist: Boolean
            ): FileHandle = RecordingFileHandle(
                delegate = super.openReadWrite(file = file, mustCreate = mustCreate, mustExist = mustExist),
                onFlush = { steps += "flush" }
            )

            override fun atomicMove(
                source: Path,
                target: Path
            ) {
                steps += "move"
                super.atomicMove(source = source, target = target)
            }
        }

        store(fileSystem = recording).write(vaultPath = VAULT, path = NOTE, content = TEXT)

        assertEquals(expected = listOf("flush", "move"), actual = steps)
        assertEquals(expected = TEXT, actual = store().read(vaultPath = VAULT, path = NOTE))
    }

    @Test
    fun `the temporary file takes the note's attributes before it replaces the note`() = runStoreTest {
        writeNote(content = ORIGINAL)
        var noteAtCopy: String? = null
        attributes.onCopy = { noteAtCopy = fakeFileSystem.read(VAULT.toPath() / NOTE.value) { readUtf8() } }

        store().write(vaultPath = VAULT, path = NOTE, content = TEXT)

        assertEquals(expected = "$VAULT/${NOTE.value}", actual = attributes.copies.single().first)
        assertEquals(expected = "$VAULT/$FOLDER", actual = attributes.copies.single().second.substringBeforeLast("/"))
        assertEquals(expected = ORIGINAL, actual = noteAtCopy)
    }

    @Test
    fun `a longer temporary file left by a crash leaves nothing of itself in the next save`() = runStoreTest {
        writeNote(content = ORIGINAL)
        fakeFileSystem.write(file = VAULT.toPath() / FOLDER / ".Arepas.md.notekea-tmp") { writeUtf8(TEXT + TEXT) }

        store().write(vaultPath = VAULT, path = NOTE, content = TEXT)

        assertEquals(expected = TEXT, actual = store().read(vaultPath = VAULT, path = NOTE))
    }

    @Test
    fun `a note that is gone says so, and reads as null for who only needs the text`() = runStoreTest {
        val gone = RelativePath(value = "Gone.md")

        assertEquals(
            expected = Outcome.Failure(error = NotesError.EntryMissing(path = "Gone.md")) to null,
            actual = store().readText(vaultPath = VAULT, path = gone) to store().read(vaultPath = VAULT, path = gone)
        )
    }

    @Test
    fun `a note in another encoding is never read as text, so it is never rewritten with replaced bytes`() =
        runStoreTest {
            val latin1 = byteArrayOf('c'.code.toByte(), 'a'.code.toByte(), 'f'.code.toByte(), 0xE9.toByte())
            fakeFileSystem.write(file = VAULT.toPath() / NOTE.value) { write(latin1) }

            assertEquals(
                expected = Outcome.Failure(error = NotesError.Unreadable(path = NOTE.value)),
                actual = store().readText(vaultPath = VAULT, path = NOTE)
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
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
        attributes = attributes
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
