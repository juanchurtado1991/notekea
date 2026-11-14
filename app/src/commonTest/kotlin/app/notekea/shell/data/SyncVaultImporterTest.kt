package app.notekea.shell.data

import app.notekea.core.domain.Outcome
import app.notekea.sync.domain.FakeVaultSync
import app.notekea.sync.domain.SyncError
import app.notekea.vault.domain.VaultError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A vault brought from a repository through the sync feature (the developer's request of 2026-11-14). */
class SyncVaultImporterTest {
    private val sync = FakeVaultSync()
    private val asked = mutableListOf<String>()

    private val importer = SyncVaultImporter(
        vaultSyncs = { vaultPath ->
            asked += vaultPath
            sync
        }
    )

    private suspend fun import(url: String = URL) = importer.import(
        vaultPath = VAULT,
        remoteUrl = url,
        token = TOKEN
    )

    @Test
    fun `the folder is connected to the repository with its token, as Settings connects one`() =
        runTest {
            assertEquals(
                expected = listOf<Any>(Outcome.Success(value = Unit), listOf(VAULT), listOf("connect $URL $TOKEN")),
                actual = listOf<Any>(import(), asked, sync.calls)
            )
        }

    @Test
    fun `what is no address of a repository is never asked of Git`() = runTest {
        assertEquals(
            expected = listOf<Any>(
                Outcome.Failure(error = VaultError.RepositoryBadAddress),
                emptyList<String>()
            ),
            actual = listOf<Any>(import(url = "github.com/ana/notes"), sync.calls)
        )
    }

    @Test
    fun `each way a repository cannot be brought is said as its own problem`() = runTest {
        val problems = listOf(
            SyncError.AuthFailed,
            SyncError.Offline,
            SyncError.BadAddress,
            SyncError.Failed(message = "not a repository")
        ).map { error ->
            sync.connectFailure = error
            (import() as Outcome.Failure).error
        }

        assertEquals(
            expected = listOf(
                VaultError.RepositoryRefused,
                VaultError.RepositoryOffline,
                VaultError.RepositoryBadAddress,
                VaultError.RepositoryFailed(message = "not a repository")
            ),
            actual = problems
        )
    }

    private companion object {
        const val VAULT = "/home/ana/Documents/notes"
        const val URL = "https://github.com/ana/notes.git"
        const val TOKEN = "token"
    }
}
