package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultListing
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
import kotlin.time.Instant

class OkioNoteScannerTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeFileSystem = FakeFileSystem().apply { allowSymlinks = true }

    @Test
    fun `notes and folders are listed recursively with their size and date`() = runScannerTest {
        writeNote(path = "Welcome.md")
        writeNote(path = "Projects/Garden/Tomato plan.md")
        writeNote(path = "Recipes/README.MD")

        assertEquals(
            expected = Outcome.Success(
                value = VaultListing(
                    folders = paths("Projects", "Projects/Garden", "Recipes"),
                    files = listOf("Projects/Garden/Tomato plan.md", "Recipes/README.MD", "Welcome.md").map {
                        expectedNoteFile(path = it)
                    }
                )
            ),
            actual = sortedFiles(outcome = scanner().scan(vaultPath = VAULT))
        )
    }

    @Test
    fun `hidden entries and links are skipped, other files listed apart, empty folders kept`() = runScannerTest {
        listOf(".obsidian/app.md", ".trash/old.md", "Ideas/.draft.md", "attachments/shopping.txt").forEach {
            writeNote(path = it)
        }
        fakeFileSystem.createDirectories(dir = vaultPath(path = "Empty"))
        writeNote(path = "Ideas/Reading list.md")
        fakeFileSystem.createSymlink(source = vaultPath(path = "Ideas/loop"), target = VAULT.toPath())
        fakeFileSystem.createSymlink(
            source = vaultPath(path = "link.md"),
            target = vaultPath(path = "Ideas/Reading list.md")
        )

        assertEquals(
            expected = Outcome.Success(
                value = VaultListing(
                    folders = paths("Empty", "Ideas", "attachments"),
                    files = listOf(expectedNoteFile(path = "Ideas/Reading list.md")),
                    attachments = listOf(expectedNoteFile(path = "attachments/shopping.txt"))
                )
            ),
            actual = sortedFiles(outcome = scanner().scan(vaultPath = VAULT))
        )
    }

    @Test
    fun `a vault that is a symbolic link is followed`() = runScannerTest {
        writeNote(path = "Welcome.md")
        fakeFileSystem.createSymlink(source = LINKED_VAULT.toPath(), target = VAULT.toPath())

        assertEquals(
            expected = Outcome.Success(
                value = VaultListing(folders = emptySet(), files = listOf(expectedNoteFile(path = "Welcome.md")))
            ),
            actual = scanner().scan(vaultPath = LINKED_VAULT)
        )
    }

    @Test
    fun `a missing vault, a file or a dangling link is missing and an unreadable vault is denied`() = runScannerTest {
        writeNote(path = "Welcome.md")
        fakeFileSystem.createSymlink(source = LINKED_VAULT.toPath(), target = MISSING.toPath())
        val file = "$VAULT/Welcome.md"

        assertEquals(
            expected = listOf(MISSING, file, LINKED_VAULT).map {
                Outcome.Failure(error = NotesError.VaultMissing(path = it))
            },
            actual = listOf(MISSING, file, LINKED_VAULT).map { scanner().scan(vaultPath = it) }
        )
        assertEquals(
            expected = Outcome.Failure(error = NotesError.PermissionDenied(path = VAULT)),
            actual = scanner(fileSystem = unlistable(folder = VAULT.toPath())).scan(vaultPath = VAULT)
        )
    }

    @Test
    fun `a subfolder that cannot be listed is shown empty`() = runScannerTest {
        writeNote(path = "Welcome.md")
        writeNote(path = "Private/secret.md")

        assertEquals(
            expected = Outcome.Success(
                value = VaultListing(folders = paths("Private"), files = listOf(expectedNoteFile(path = "Welcome.md")))
            ),
            actual = scanner(fileSystem = unlistable(folder = vaultPath(path = "Private"))).scan(vaultPath = VAULT)
        )
    }

    @Test
    fun `stat describes a visible note and nothing else`() = runScannerTest {
        listOf("Welcome.md", ".obsidian/app.md", "attachments/shopping.txt").forEach { writeNote(path = it) }
        fakeFileSystem.createDirectories(dir = vaultPath(path = "Folder.md"))

        assertEquals(
            expected = listOf(expectedNoteFile(path = "Welcome.md"), null, null, null, null),
            actual = listOf("Welcome.md", ".obsidian/app.md", "attachments/shopping.txt", "Folder.md", "Gone.md").map {
                scanner().stat(vaultPath = VAULT, path = RelativePath(value = it))
            }
        )
    }

    @Test
    fun `statAttachment describes a visible file that is not a note and nothing else`() = runScannerTest {
        listOf("Welcome.md", ".obsidian/app.json", "attachments/plate.png").forEach { writeNote(path = it) }
        fakeFileSystem.createDirectories(dir = vaultPath(path = "Photos.png"))

        assertEquals(
            expected = listOf(expectedNoteFile(path = "attachments/plate.png"), null, null, null, null),
            actual = listOf("attachments/plate.png", "Welcome.md", ".obsidian/app.json", "Photos.png", "Gone.png").map {
                scanner().statAttachment(vaultPath = VAULT, path = RelativePath(value = it))
            }
        )
    }

    private fun expectedNoteFile(
        path: String
    ): NoteFile {
        val metadata = fakeFileSystem.metadata(path = vaultPath(path = path))
        return NoteFile(
            path = RelativePath(value = path),
            size = metadata.size,
            modifiedAt = metadata.lastModifiedAtMillis?.let { Instant.fromEpochMilliseconds(epochMilliseconds = it) }
        )
    }

    private fun paths(
        vararg values: String
    ): Set<RelativePath> = values.map { RelativePath(value = it) }.toSet()

    private fun runScannerTest(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { block() }

    private fun scanner(
        fileSystem: FileSystem = fakeFileSystem
    ): OkioNoteScanner = OkioNoteScanner(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    private fun sortedFiles(
        outcome: Outcome<VaultListing, NotesError>
    ): Outcome<VaultListing, NotesError> = when (outcome) {
        is Outcome.Failure -> outcome

        is Outcome.Success -> Outcome.Success(
            value = outcome.value.copy(
                files = outcome.value.files.sortedBy {
                    it.path.value
                }
            )
        )
    }

    private fun unlistable(
        folder: Path
    ): FileSystem = object : ForwardingFileSystem(delegate = fakeFileSystem) {
        override fun list(
            dir: Path
        ): List<Path> = if (dir == folder) throw IOException(DENIED) else super.list(dir = dir)

        override fun listOrNull(
            dir: Path
        ): List<Path>? = if (dir == folder) null else super.listOrNull(dir = dir)
    }

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
        private const val DENIED = "Permission denied"
        private const val LINKED_VAULT = "/home/ana/Linked"
        private const val MISSING = "/home/ana/Missing"
        private const val VAULT = "/home/ana/Notes"
    }
}
