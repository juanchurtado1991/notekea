package app.notekea.vault.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/** A vault brought from a Git repository, from the welcome screen (the developer's request of 2026-11-14). */
class ImportVaultUseCaseTest {
    private val fileSystem = FakeVaultFileSystem()
    private val recents = FakeRecentVaultsStore()
    private val imports = mutableListOf<List<String?>>()
    private var failure: VaultError? = null

    private val import = ImportVaultUseCase(
        vaultFileSystem = fileSystem,
        validateFileName = ValidateFileNameUseCase(),
        importer = object : VaultImporter {
            override fun isAddress(remoteUrl: String): Boolean =
                remoteUrl.startsWith(prefix = "https://")

            override suspend fun import(
                vaultPath: String,
                remoteUrl: String,
                token: String?
            ): Outcome<Unit, VaultError> {
                imports += listOf(vaultPath, remoteUrl, token)

                return failure?.let { Outcome.Failure(error = it) } ?: Outcome.Success(value = Unit)
            }
        },
        openVault = OpenVaultUseCase(
            vaultFileSystem = fileSystem,
            recentVaultsStore = recents,
            clock = Clock.System
        )
    )

    private suspend fun run(
        name: String = "notes",
        url: String = " https://github.com/ana/notes.git ",
        token: String? = " "
    ) = import(
        parentPath = FakeVaultFileSystem.DEFAULT_PARENT,
        name = name,
        remoteUrl = url,
        token = token
    )

    @Test
    fun `the repository is brought into a new folder, which opens as the vault`() = runTest {
        val vault = run()
        val folder = "${FakeVaultFileSystem.DEFAULT_PARENT}/notes"

        assertEquals(
            expected = listOf<Any?>(
                folder,
                listOf(listOf(folder, "https://github.com/ana/notes.git", null))
            ),
            actual = listOf<Any?>(
                (vault as Outcome.Success).value.path,
                imports
            )
        )
    }

    @Test
    fun `a repository that cannot be brought leaves no folder behind, and says why`() = runTest {
        failure = VaultError.RepositoryRefused
        val folder = "${FakeVaultFileSystem.DEFAULT_PARENT}/notes"

        assertEquals(
            expected = listOf<Any?>(
                Outcome.Failure(error = VaultError.RepositoryRefused),
                listOf(folder)
            ),
            actual = listOf<Any?>(run(), fileSystem.discarded)
        )
    }

    @Test
    fun `a folder that holds files is never imported into, and no address is asked of Git`() =
        runTest {
            val folder = "${FakeVaultFileSystem.DEFAULT_PARENT}/notes"
            fileSystem.filled += folder

            val taken = run()
            val unnamed = run(name = "a/b")
            val noAddress = run(url = "  ")
            val notOne = run(url = "github.com/ana/notes")

            assertEquals(
                expected = listOf<Any?>(
                    Outcome.Failure(error = VaultError.FolderAlreadyExists(path = folder)),
                    Outcome.Failure(error = VaultError.InvalidName),
                    Outcome.Failure(error = VaultError.RepositoryBadAddress),
                    emptyList<Any>()
                ),
                actual = listOf<Any?>(taken, unnamed, noAddress, imports)
            )
        }

    @Test
    fun `the folder is suggested after the repository's name`() {
        assertEquals(
            expected = listOf("notes", "notes", "vault"),
            actual = listOf(
                ImportVaultUseCase.nameOf(remoteUrl = "https://github.com/ana/notes.git"),
                ImportVaultUseCase.nameOf(remoteUrl = "https://github.com/ana/notes/"),
                ImportVaultUseCase.nameOf(remoteUrl = "file:/home/ana/vault")
            )
        )
    }
}
