package app.notekea.sync.data

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.FakeGitRepository
import app.notekea.sync.domain.FakeSyncSettingsStore
import app.notekea.sync.domain.FakeTokenStore
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus
import app.notekea.sync.domain.UnfinishedSync
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The bug hunt of 2026-11-17: connecting or settling a conflict on a vault cut short in a merge committed Git's
 * conflict marks, and a conflict that could not be settled was not said.
 */
class SyncEngineInterruptedTest {
    private val repository = FakeGitRepository()
    private val settings = FakeSyncSettingsStore()

    private fun TestScope.engine(): SyncEngine = SyncEngine(
        vaultPath = VAULT,
        repository = repository,
        tokens = FakeTokenStore(),
        settingsStore = settings,
        edits = { MutableSharedFlow() },
        scope = backgroundScope
    )

    @Test
    fun `connecting a vault left in the middle of a merge commits nothing and says it waits for a repair`() =
        runTest {
            val engine = engine()
            repository.unfinished = UnfinishedSync(changedHere = listOf(PLAN))

            val connected = engine.connect(
                settings = SyncSettings(remoteUrl = URL),
                token = null
            )

            assertEquals(
                expected = listOf<Any?>(true, INTERRUPTED, emptyList<String>(), null),
                actual = listOf(
                    connected is Outcome.Failure,
                    engine.status.value,
                    repository.calls.toList(),
                    settings.settings[VAULT]
                )
            )
        }

    @Test
    fun `settling a conflict in a vault left in the middle of a merge waits for the repair`() =
        runTest {
            val engine = engine()

            engine.connect(
                settings = SyncSettings(remoteUrl = URL),
                token = null
            )

            repository.unfinished = UnfinishedSync(changedHere = listOf(PLAN))
            repository.calls.clear()

            engine.resolve(
                note = PLAN,
                choice = ConflictChoice.MINE
            )

            assertEquals(
                expected = INTERRUPTED to emptyList<String>(),
                actual = engine.status.value to repository.calls.toList()
            )
        }

    @Test
    fun `a conflict that could not be settled says why, and nothing is synced`() = runTest {
        val engine = engine()

        engine.connect(
            settings = SyncSettings(remoteUrl = URL),
            token = null
        )

        repository.resolveError = SyncError.Failed(message = LOCKED)
        repository.calls.clear()

        engine.resolve(
            note = PLAN,
            choice = ConflictChoice.MINE
        )

        assertEquals(
            expected = SyncStatus.Failed(message = LOCKED) to listOf("resolve Plan.md MINE"),
            actual = engine.status.value to repository.calls.toList()
        )
    }

    private companion object {
        const val VAULT = "/vaults/Notes"
        const val URL = "https://example.com/notes.git"
        const val LOCKED = "The note is open in another program"
        val PLAN = RelativePath(value = "Plan.md")
        val INTERRUPTED = SyncStatus.Interrupted(changedHere = listOf(PLAN))
    }
}
