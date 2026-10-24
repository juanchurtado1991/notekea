package app.notekea.vault.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.vault.domain.FolderAccess
import app.notekea.vault.domain.VaultError
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

class OkioVaultFileSystemTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeFileSystem = FakeFileSystem()

    @Test
    fun `a folder is available, missing, or not readable`() = runVaultTest {
        fakeFileSystem.createDirectories(dir = NOTES.toPath())
        fakeFileSystem.write(file = FILE.toPath()) { writeUtf8(string = CONTENT) }

        assertEquals(
            expected = listOf(FolderAccess.AVAILABLE, FolderAccess.MISSING, FolderAccess.MISSING),
            actual = listOf(NOTES, FILE, MISSING).map { vaultFileSystem().access(path = it) }
        )
        assertEquals(
            expected = FolderAccess.NOT_READABLE,
            actual = vaultFileSystem(fileSystem = unlistable()).access(path = NOTES)
        )
    }

    @Test
    fun `a new folder is created, or reused when it exists and is empty`() = runVaultTest {
        fakeFileSystem.createDirectories(dir = DOCUMENTS.toPath())

        val created = vaultFileSystem().createFolder(parentPath = DOCUMENTS, name = NAME)
        val reused = vaultFileSystem().createFolder(parentPath = DOCUMENTS, name = NAME)

        assertEquals(
            expected = listOf(Outcome.Success(value = NEW_VAULT), Outcome.Success(value = NEW_VAULT)),
            actual = listOf(created, reused)
        )
    }

    @Test
    fun `creating fails for a folder with files, a missing parent or a disk error`() = runVaultTest {
        writeFile(path = "$NEW_VAULT/a.md")

        assertEquals(
            expected = Outcome.Failure(error = VaultError.FolderAlreadyExists(path = NEW_VAULT)),
            actual = vaultFileSystem().createFolder(parentPath = DOCUMENTS, name = NAME)
        )
        assertEquals(
            expected = Outcome.Failure(error = VaultError.FolderMissing(path = MISSING)),
            actual = vaultFileSystem().createFolder(parentPath = MISSING, name = NAME)
        )
        assertEquals(
            expected = Outcome.Failure(error = VaultError.CreationFailed(path = "$NOTES/$NAME")),
            actual = vaultFileSystem(fileSystem = readOnly()).createFolder(parentPath = NOTES, name = NAME)
        )
    }

    @Test
    fun `new vaults are suggested in Documents, or in the home folder without one`() = runVaultTest {
        fakeFileSystem.createDirectories(dir = HOME.toPath())

        val withoutDocuments = vaultFileSystem().suggestedParentFolder()
        fakeFileSystem.createDirectories(dir = DOCUMENTS.toPath())

        assertEquals(
            expected = listOf(HOME, DOCUMENTS),
            actual = listOf(withoutDocuments, vaultFileSystem().suggestedParentFolder())
        )
    }

    private fun readOnly(): FileSystem {
        fakeFileSystem.createDirectories(dir = NOTES.toPath())
        return object : ForwardingFileSystem(delegate = fakeFileSystem) {
            override fun createDirectory(
                dir: Path,
                mustCreate: Boolean
            ) = throw IOException(DENIED)
        }
    }

    private fun runVaultTest(
        block: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) { block() }

    private fun unlistable(): FileSystem {
        fakeFileSystem.createDirectories(dir = NOTES.toPath())
        return object : ForwardingFileSystem(delegate = fakeFileSystem) {
            override fun list(
                dir: Path
            ): List<Path> = throw IOException(DENIED)
        }
    }

    private fun vaultFileSystem(
        fileSystem: FileSystem = fakeFileSystem
    ): OkioVaultFileSystem = OkioVaultFileSystem(
        fileSystem = fileSystem,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher),
        homeDirectory = HOME
    )

    private fun writeFile(
        path: String
    ) {
        fakeFileSystem.createDirectories(dir = path.toPath().parent!!)
        fakeFileSystem.write(file = path.toPath()) { writeUtf8(string = CONTENT) }
    }

    companion object {
        private const val CONTENT = "# Note"
        private const val DENIED = "Permission denied"
        private const val HOME = "/home/ana"
        private const val DOCUMENTS = "$HOME/Documents"
        private const val FILE = "$HOME/notes.txt"
        private const val MISSING = "$HOME/Missing"
        private const val NAME = "My Notes"
        private const val NEW_VAULT = "$DOCUMENTS/$NAME"
        private const val NOTES = "$HOME/Notes"
    }
}
