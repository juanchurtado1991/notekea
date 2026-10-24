package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.DeleteEntryUseCase
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.GetTreePreferencesUseCase
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.RevealEntryUseCase
import app.notekea.notes.domain.SaveTreePreferencesUseCase
import app.notekea.notes.domain.TreePreferences
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlin.time.TimeSource

/**
 * Vault sidebar (`ROADMAP.md` 1.4): shows the vault tree, kept up to date by the file watcher, and runs the actions
 * on its entries. The expanded folders and the sort are restored when the vault opens and saved on every change. The
 * rows are flattened again whenever the tree, the expanded folders or the sort change. Renaming can be confirmed twice
 * at once (Enter and the field losing focus); only the first confirmation runs. After a rename, the notes that link to
 * the entry are updated in the background, as Obsidian does by default, so no link breaks; a notice says how many
 * notes changed. A choice to keep old links ("Update links when renaming") belongs to Settings (`ROADMAP.md` 5.2).
 */
class VaultTreeViewModel(
    private val vaultPath: String,
    platformConventions: PlatformConventions,
    private val observeVaultTree: ObserveVaultTreeUseCase,
    private val flattenTree: FlattenTreeUseCase,
    private val createNote: CreateNoteUseCase,
    private val createFolder: CreateFolderUseCase,
    private val renameEntry: RenameEntryUseCase,
    private val deleteEntry: DeleteEntryUseCase,
    private val revealEntry: RevealEntryUseCase,
    private val planLinkUpdates: PlanLinkUpdatesUseCase,
    private val applyLinkUpdates: ApplyLinkUpdatesUseCase,
    private val getTreePreferences: GetTreePreferencesUseCase,
    private val saveTreePreferences: SaveTreePreferencesUseCase
) : BaseViewModel<VaultTreeState, VaultTreeIntent, Nothing>(
    initialState = VaultTreeState(vaultPath = vaultPath, platformConventions = platformConventions)
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var renameJob: Job? = null

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            val preferences = getTreePreferences(vaultPath = vaultPath)
            updateTree { restorePreferences(state = it, preferences = preferences) }
            observe()
        }
    }

    override fun onIntent(
        intent: VaultTreeIntent
    ) {
        when (intent) {
            is VaultTreeIntent.OnChevronClick -> updateTree { reduceToggleFolder(state = it, folder = intent.path) }
            VaultTreeIntent.OnCollapseSelected -> updateTree(transform = ::reduceCollapseSelected)
            VaultTreeIntent.OnDeleteCancel -> reduce { it.copy(pendingDelete = null) }
            VaultTreeIntent.OnDeleteConfirm -> delete()
            is VaultTreeIntent.OnDeleteRequest -> reduce { it.copy(pendingDelete = intent.path ?: it.selectedPath) }
            VaultTreeIntent.OnErrorDismiss -> reduce { it.copy(actionError = null, hasUnexpectedError = false) }
            VaultTreeIntent.OnExpandSelected -> updateTree(transform = ::reduceExpandSelected)
            is VaultTreeIntent.OnNewFolderClick -> create(baseName = intent.baseName, isFolder = true)
            is VaultTreeIntent.OnNewNoteClick -> create(baseName = intent.baseName, isFolder = false)
            VaultTreeIntent.OnNoticeDismiss -> reduce { it.copy(notice = null) }
            VaultTreeIntent.OnRenameCancel -> reduce { it.copy(renaming = null) }
            VaultTreeIntent.OnRenameConfirm -> rename()
            is VaultTreeIntent.OnRenameRequest -> startRename(path = intent.path)
            is VaultTreeIntent.OnRenameTextChange -> changeRenameText(text = intent.text)
            VaultTreeIntent.OnRetryClick -> retry()
            is VaultTreeIntent.OnRevealRequest -> reveal(path = intent.path)
            is VaultTreeIntent.OnRowClick -> updateTree { reduceRowClick(state = it, path = intent.path) }
            is VaultTreeIntent.OnRowSelect -> reduce { it.copy(selectedPath = intent.path) }
            VaultTreeIntent.OnSelectionClear -> reduce { it.copy(selectedPath = null) }
            VaultTreeIntent.OnSelectNext -> reduce { reduceSelectionMove(state = it, step = 1) }
            VaultTreeIntent.OnSelectPrevious -> reduce { reduceSelectionMove(state = it, step = -1) }
            is VaultTreeIntent.OnSidebarResize -> resizeSidebar(widthDp = intent.widthDp)
            VaultTreeIntent.OnSidebarToggle -> updateTree { it.copy(isSidebarCollapsed = !it.isSidebarCollapsed) }
            is VaultTreeIntent.OnSortChange -> updateTree { it.copy(sort = intent.sort) }
        }
    }

    private fun changeRenameText(
        text: String
    ) {
        reduce { it.copy(renaming = it.renaming?.copy(text = text, error = null)) }
    }

    private fun create(
        baseName: String,
        isFolder: Boolean
    ) {
        launchGuarded(onError = ::showUnexpectedError) {
            val folder = targetFolder(state = state.value)
            val outcome = if (isFolder) {
                createFolder(vaultPath = vaultPath, folder = folder, baseName = baseName)
            } else {
                createNote(vaultPath = vaultPath, folder = folder, baseName = baseName)
            }
            updateTree { reduceCreated(state = it, outcome = outcome, isFolder = isFolder) }
        }
    }

    private fun delete() {
        val path = state.value.pendingDelete ?: return
        launchGuarded(onError = ::showUnexpectedError) {
            val outcome = deleteEntry(vaultPath = vaultPath, path = path)
            updateTree { reduceDeleted(state = it, path = path, outcome = outcome) }
        }
    }

    private suspend fun observe() {
        val start = TimeSource.Monotonic.markNow()
        var isFirstTree = true
        observeVaultTree(vaultPath = vaultPath).collect { outcome ->
            when (outcome) {
                is Outcome.Failure -> {
                    reduce { reduceLoadFailed(state = it, error = outcome.error) }
                }

                is Outcome.Success -> {
                    if (isFirstTree) {
                        logger.i {
                            "Vault tree loaded: ${outcome.value.notes.size} notes in ${start.elapsedNow()}"
                        }
                    }
                    isFirstTree = false
                    updateTree { reduceTreeLoaded(state = it, tree = outcome.value) }
                }
            }
        }
    }

    private fun rename() {
        val draft = state.value.renaming ?: return
        if (renameJob?.isActive == true) return
        renameJob = launchGuarded(onError = ::showUnexpectedError) {
            val notesBefore = state.value.tree.notes.keys
            val outcome = renameEntry(vaultPath = vaultPath, path = draft.path, newName = draft.text)
            updateTree { reduceRenameOutcome(state = it, from = draft.path, outcome = outcome) }
            if (outcome is Outcome.Success && outcome.value != draft.path) {
                updateLinks(notesBefore = notesBefore, from = draft.path, to = outcome.value)
            }
        }
    }

    /** Rewrites the links to [from] in the notes that had them, now that it is [to]; nothing when none link to it. */
    private suspend fun updateLinks(
        notesBefore: Set<RelativePath>,
        from: RelativePath,
        to: RelativePath
    ) {
        val rewrites = planLinkUpdates(vaultPath = vaultPath, notesBefore = notesBefore, from = from, to = to)
        if (rewrites.isEmpty()) return
        val result = applyLinkUpdates(vaultPath = vaultPath, rewrites = rewrites)
        logger.i { "Links updated after a rename: ${result.updatedCount} notes, ${result.skipped.size} skipped" }
        reduce { reduceLinksUpdated(state = it, result = result) }
    }

    private fun resizeSidebar(
        widthDp: Int
    ) {
        updateTree { it.copy(sidebarWidthDp = TreePreferences.clampSidebarWidth(widthDp = widthDp)) }
    }

    private fun retry() {
        if (state.value.loadError == null) return
        reduce { it.copy(isLoading = it.tree.notes.isEmpty() && it.tree.folders.isEmpty(), loadError = null) }
        launchGuarded(onError = ::showUnexpectedError) { observe() }
    }

    private fun reveal(
        path: RelativePath?
    ) {
        val target = path ?: state.value.selectedPath ?: return
        launchGuarded(onError = ::showUnexpectedError) {
            val outcome = revealEntry(vaultPath = vaultPath, path = target)
            if (outcome is Outcome.Failure) reduce { it.copy(actionError = outcome.error) }
        }
    }

    private fun showUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false, hasUnexpectedError = true) }
    }

    private fun startRename(
        path: RelativePath?
    ) {
        val target = path ?: state.value.selectedPath ?: return
        reduce { reduceRenameStart(state = it, path = target) }
    }

    /** Applies [transform], flattens the rows again and saves the expanded folders and sort if they changed. */
    private fun updateTree(
        transform: (VaultTreeState) -> VaultTreeState
    ) {
        val before = state.value
        reduce { current ->
            val next = transform(current)
            next.copy(rows = flattenTree(tree = next.tree, expandedFolders = next.expandedFolders, sort = next.sort))
        }
        val after = state.value
        val preferences = preferencesOf(state = after)
        if (preferences != preferencesOf(state = before) && !after.isLoading) {
            launchGuarded(
                onError = ::showUnexpectedError
            ) { saveTreePreferences(vaultPath = vaultPath, preferences = preferences) }
        }
    }

    companion object {
        private const val LOG_TAG = "VaultTreeViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the vault tree"
    }
}
