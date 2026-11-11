package app.notekea.sync.presentation

import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.BaseViewModel
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.ConflictCopies
import app.notekea.sync.domain.VaultSyncs
import co.touchlab.kermit.Logger

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
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val engine = engines.of(vaultPath = vaultPath)

    init {
        engine.start()

        launchGuarded(onError = ::logError) {
            engine.status.collect { status ->
                reduce { it.copy(status = status) }
            }
        }
    }

    override fun handleIntent(intent: SyncStatusIntent) {
        when (intent) {
            SyncStatusIntent.OnOpenSettings -> emit(effect = SyncStatusEffect.OpenSettings)
            is SyncStatusIntent.OnReview -> review(note = intent.note)
            is SyncStatusIntent.OnChoose -> choose(choice = intent.choice)
            SyncStatusIntent.OnReviewDismiss -> reduce { it.copy(review = null) }
        }
    }

    override fun onCleared() {
        engine.stop()
        super.onCleared()
    }

    private fun review(note: RelativePath) {
        launchGuarded(onError = ::logError) {
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

        launchGuarded(onError = ::logError) {
            engine.resolve(
                note = note,
                choice = choice
            )
        }
    }

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "SyncStatusViewModel"
        const val FAILED_MESSAGE = "Sync could not be followed"
    }
}
