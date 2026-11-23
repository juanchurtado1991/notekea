package app.notekea.sync.presentation

import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.ConflictCopies
import app.notekea.sync.domain.RepairChoice
import app.notekea.sync.domain.SyncStatus
import app.notekea.sync.domain.VaultSyncs
import co.touchlab.kermit.Severity

/**
 * The open vault's sync (4.2): its engine runs while the vault is open, its status shows in the status bar, and a
 * conflict is reviewed with both versions side by side ([noteContentStore] reads them) and settled with a choice.
 */
class SyncStatusViewModel(
    private val vaultPath: String,
    engines: VaultSyncs,
    private val noteContentStore: NoteContentStore
) : BaseViewModel<SyncStatusState, SyncStatusIntent, SyncStatusEffect>(
    initialState = SyncStatusState()
) {
    private val engine = engines.of(vaultPath = vaultPath)

    init {
        engine.start()

        launchLogged(
            message = FAILED_MESSAGE,
            severity = Severity.Warn
        ) {
            engine.status.collect { status ->
                reduce {
                    it.copy(
                        status = status,
                        conflicts = conflictsAfter(
                            status = status,
                            before = it.conflicts
                        ),
                        unfinished = (status as? SyncStatus.Interrupted)?.changedHere,
                        isRepairShown = it.isRepairShown && status is SyncStatus.Interrupted
                    )
                }
            }
        }

        launchLogged(
            message = FAILED_MESSAGE,
            severity = Severity.Warn
        ) {
            engine.leftOut.collect { files ->
                reduce { it.copy(leftOut = files) }
            }
        }
    }

    override fun handleIntent(intent: SyncStatusIntent) {
        when (intent) {
            SyncStatusIntent.OnOpenSettings -> emit(effect = SyncStatusEffect.OpenSettings)
            is SyncStatusIntent.OnReview -> review(note = intent.note)
            is SyncStatusIntent.OnChoose -> choose(choice = intent.choice)
            SyncStatusIntent.OnReviewDismiss -> reduce { it.copy(review = null) }
            SyncStatusIntent.OnRepairShow -> reduce { it.copy(isRepairShown = true) }
            SyncStatusIntent.OnRepairDismiss -> reduce { it.copy(isRepairShown = false) }
            is SyncStatusIntent.OnRepair -> repair(choice = intent.choice)
            SyncStatusIntent.OnLeftOutDismiss -> engine.acknowledgeLeftOut()
        }
    }

    override fun onCleared() {
        engine.stop()
        super.onCleared()
    }

    private fun review(note: RelativePath) {
        launchLogged(
            message = FAILED_MESSAGE,
            severity = Severity.Warn
        ) {
            val theirs = noteContentStore.read(
                vaultPath = vaultPath,
                path = note
            ).orEmpty()

            val mine = noteContentStore
                .read(
                    vaultPath = vaultPath,
                    path = ConflictCopies.copyOf(path = note)
                )
                .orEmpty()

            reduce {
                it.copy(
                    review = ConflictReview(
                        note = note,
                        mine = mine,
                        theirs = theirs
                    )
                )
            }
        }
    }

    private fun choose(choice: ConflictChoice) {
        val note = state.value.review?.note ?: return
        reduce { it.copy(review = null) }

        launchLogged(
            message = FAILED_MESSAGE,
            severity = Severity.Warn
        ) {
            engine.resolve(
                note = note,
                choice = choice
            )
        }
    }

    private fun repair(choice: RepairChoice) {
        reduce { it.copy(isRepairShown = false) }

        launchLogged(
            message = FAILED_MESSAGE,
            severity = Severity.Warn
        ) { engine.repair(choice = choice) }
    }

    // A sync under way keeps the conflicts it started with; any other status says what is left.
    private fun conflictsAfter(
        status: SyncStatus,
        before: List<RelativePath>
    ): List<RelativePath> = when (status) {
        is SyncStatus.Conflict -> status.notes
        SyncStatus.Syncing -> before
        else -> emptyList()
    }

    private companion object {
        const val FAILED_MESSAGE = "Sync could not be followed"
    }
}
