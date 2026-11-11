package app.notekea.sync.presentation

import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.FakeVaultSync
import app.notekea.sync.domain.SyncStatus
import app.notekea.sync.domain.VaultSyncs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The open vault's sync in the status bar and the conflict banner. */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncStatusViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val sync = FakeVaultSync()
    private val store = FakeNoteContentStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // The smoke of 4.2-02: the conflict copy is itself an edit, so the vault syncs again while the banner shows.
    @Test
    fun `a conflict stays on the banner and under review while the vault syncs again`() =
        runTest(dispatcher) {
            store.texts[PLAN] = "theirs"
            store.texts[RelativePath(value = "Plan (conflict).md")] = "mine"

            val viewModel = SyncStatusViewModel(
                vaultPath = VAULT,
                engines = VaultSyncs { sync },
                noteContentStore = store
            )

            sync.status.value = SyncStatus.Conflict(notes = listOf(PLAN))
            advanceUntilIdle()

            viewModel.onIntent(intent = SyncStatusIntent.OnReview(note = PLAN))
            sync.status.value = SyncStatus.Syncing
            advanceUntilIdle()
            val whileSyncing = viewModel.state.value

            viewModel.onIntent(intent = SyncStatusIntent.OnChoose(choice = ConflictChoice.MINE))
            sync.status.value = SyncStatus.UpToDate
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(
                    listOf(PLAN),
                    ConflictReview(
                        note = PLAN,
                        mine = "mine",
                        theirs = "theirs"
                    ),
                    emptyList<RelativePath>(),
                    null,
                    listOf("start", "resolve Plan.md MINE")
                ),
                actual = listOf(
                    whileSyncing.conflicts,
                    whileSyncing.review,
                    viewModel.state.value.conflicts,
                    viewModel.state.value.review,
                    sync.calls
                )
            )
        }

    private companion object {
        const val VAULT = "/vaults/Notes"
        val PLAN = RelativePath(value = "Plan.md")
    }
}
