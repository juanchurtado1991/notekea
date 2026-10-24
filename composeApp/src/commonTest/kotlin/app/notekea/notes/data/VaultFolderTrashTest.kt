package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TrashLocation
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

class VaultFolderTrashTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeFileSystem = FakeFileSystem()

    @Test
    fun `notes and folders move into the vault trash folder with their content`() = runTrashTest {
        writeNote(path = "Recipes/Arepas.md")
        writeNote(path = "Travel/Lisbon trip.md")

        val outcomes = listOf(
            "Recipes/Arepas.md",
            "Travel"
        ).map { trash().moveToTrash(vaultPath = VAULT, path = path(it)) }

        assertEquals(
            expected = List(size = 2) { Outcome.Success(value = TrashLocation.VAULT_TRASH_FOLDER) },
            actual = outcomes
        )
        assertEquals(
            expected = listOf(false, false, CONTENT, CONTENT),
            actual = listOf(
                fakeFileSystem.exists(path = vaultPath(path = "Recipes/Arepas.md")),
                fakeFileSystem.exists(path = vaultPath(path = "Travel")),
                fakeFileSystem.read(file = vaultPath(path = ".trash/Arepas.md")) { readUtf8() },
                fakeFileSystem.read(file = vaultPath(path = ".trash/Travel/Lisbon trip.md")) { readUtf8() }
            )
        )
    }

    @Test
    fun `a name already in the trash gets the next free number`() = runTrashTest {
        listOf(
            ".trash/Arepas.md",
            ".trash/Arepas 2.md",
            "Arepas.md",
            "Recipes/Arepas.md"
        ).forEach { writeNote(path = it) }

        listOf("Arepas.md", "Recipes/Arepas.md").forEach { trash().moveToTrash(vaultPath = VAULT, path = path(it)) }

        assertEquals(
            expected = listOf("Arepas 2.md", "Arepas 3.md", "Arepas 4.md", "Arepas.md"),
            actual = fakeFileSystem.list(dir = vaultPath(path = ".trash")).map { it.name }.sorted()
        )
    }

    @Test
    fun `a missing entry or a refusing disk fails`() = runTrashTest {
        writeNote(path = "Arepas.md")

        assertEquals(
            expected = listOf(
                Outcome.Failure(error = NotesError.EntryMissing(path = "Gone.md")),
                Outcome.Failure(error = NotesError.OperationFailed(path = "Arepas.md"))
            ),
            actual = listOf(
                trash().moveToTrash(vaultPath = VAULT, path = path("Gone.md")),
                trash(fileSystem = refusing()).moveToTrash(vaultPath = VAULT, path = path("Arepas.md"))
            )
        )
    }

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    private fun refusing(): FileSystem = object : ForwardingFileSystem(delegate = fakeFileSystem) {
        override fun atomicMove(
            source: Path,
            target: Path
        ) = throw IOException(DENIED)
    }

    private fun runTrashTest(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { block() }

    private fun trash(
        fileSystem: FileSystem = fakeFileSystem
    ): VaultFolderTrash = VaultFolderTrash(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    private fun vaultPath(
        path: String
    ): Path = VAULT.toPath() / path

    private fun writeNote(
        path: String
    ) {
        val file = vaultPath(path = path)
        file.parent?.let { fakeFileSystem.createDirectories(dir = it) }
        fakeFileSystem.write(file = file) { writeUtf8(string = CONTENT) }
    }

    companion object {
        private const val CONTENT = "# Note\n"
        private const val DENIED = "Read-only file system"
        private const val VAULT = "/home/ana/Notes"
    }
}
