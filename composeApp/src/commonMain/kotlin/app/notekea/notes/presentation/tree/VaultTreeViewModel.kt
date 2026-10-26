package app.notekea.notes.presentation.tree

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.AttachmentStore
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.DeleteEntryUseCase
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.FollowLinkUseCase
import app.notekea.notes.domain.GetTreePreferencesUseCase
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.RevealEntryUseCase
import app.notekea.notes.domain.SaveTreePreferencesUseCase
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
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
 * A selected file that is not a note (an image, a PDF) is read and shown beside the tree, and opens with its app
 * (`ROADMAP.md` 1.8).
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
    private val updateLinksAfterRename: UpdateLinksAfterRenameUseCase,
    private val followLink: FollowLinkUseCase,
    private val getTreePreferences: GetTreePreferencesUseCase,
    private val saveTreePreferences: SaveTreePreferencesUseCase,
    private val fileOpener: FileOpener,
    attachmentStore: AttachmentStore,
    dispatchers: AppDispatchers
) : BaseViewModel<VaultTreeState, VaultTreeIntent, VaultTreeEffect>(
    initialState = VaultTreeState(vaultPath = vaultPath, platformConventions = platformConventions)
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var renameJob: Job? = null
    private val filePreviews =
        TreeFilePreviews(vaultPath = vaultPath, attachmentStore = attachmentStore, dispatchers = dispatchers)
    private var previewJob: Job? = null
    private var previewed: NoteFile? = null

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
            is VaultTreeIntent.OnEntryRenamed -> entryRenamed(intent = intent)
            is VaultTreeIntent.OnDeleteRequest -> reduce { it.copy(pendingDelete = intent.path ?: it.selectedPath) }
            VaultTreeIntent.OnErrorDismiss -> reduce { it.copy(actionError = null, hasUnexpectedError = false) }
            VaultTreeIntent.OnExpandSelected -> updateTree(transform = ::reduceExpandSelected)
            is VaultTreeIntent.OnFollowLink -> follow(intent = intent)
            is VaultTreeIntent.OnNewFolderClick -> create(baseName = intent.baseName, isFolder = true)
            is VaultTreeIntent.OnNewNoteClick -> create(baseName = intent.baseName, isFolder = false)
            VaultTreeIntent.OnNoticeDismiss -> reduce { it.copy(notice = null) }
            VaultTreeIntent.OnRenameCancel -> reduce { it.copy(renaming = null) }
            VaultTreeIntent.OnRenameConfirm -> rename()
            is VaultTreeIntent.OnRenameRequest -> startRename(path = intent.path)
            is VaultTreeIntent.OnRenameTextChange -> changeRenameText(text = intent.text)
            VaultTreeIntent.OnRetryClick -> retry()
            is VaultTreeIntent.OnRevealNote -> updateTree { reduceReveal(state = it, path = intent.path) }
            is VaultTreeIntent.OnRevealRequest -> reveal(path = intent.path)
            is VaultTreeIntent.OnRowClick -> updateTree { reduceRowClick(state = it, path = intent.path) }
            is VaultTreeIntent.OnRowSelect -> reduce { it.copy(selectedPath = intent.path) }
            VaultTreeIntent.OnSelectionClear -> reduce { it.copy(selectedPath = null) }
            VaultTreeIntent.OnSelectNext -> reduce { reduceSelectionMove(state = it, step = 1) }
            VaultTreeIntent.OnSelectPrevious -> reduce { reduceSelectionMove(state = it, step = -1) }
            is VaultTreeIntent.OnSidebarResize -> resizeSidebar(widthDp = intent.widthDp)
            VaultTreeIntent.OnSidebarToggle -> updateTree { it.copy(isSidebarCollapsed = !it.isSidebarCollapsed) }
            is VaultTreeIntent.OnSortChange -> updateTree { it.copy(sort = intent.sort) }
            is VaultTreeIntent.OnFileOpen -> openFile(path = intent.path)
        }
        updatePreview()
    }

    private fun openFile(
        path: RelativePath?
    ) {
        val target = path ?: state.value.selectedPath ?: return
        launchGuarded(onError = ::showUnexpectedError) {
            val outcome = fileOpener.open(vaultPath = vaultPath, path = target)
            if (outcome is Outcome.Failure) reduce { it.copy(actionError = outcome.error) }
        }
    }

    /** Reads the selected file that is not a note, again when it changed on disk; clears it when none is selected. */
    private fun updatePreview() {
        val file = state.value.selectedPath?.let { state.value.tree.attachments[it] }
        if (file == previewed) return
        previewed = file
        previewJob?.cancel()
        if (file == null) {
            reduce { it.copy(filePreview = null) }
            return
        }
        previewJob = launchGuarded(onError = ::showUnexpectedError) {
            val preview = filePreviews.of(file = file)
            reduce { if (it.selectedPath == file.path) it.copy(filePreview = preview) else it }
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
                        logger.i { TREE_LOADED_MESSAGE + outcome.value.notes.size + NOTES_IN + start.elapsedNow() }
                    }
                    isFirstTree = false
                    updateTree { reduceTreeLoaded(state = it, tree = outcome.value) }
                    updatePreview()
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
            val isMoved = outcome is Outcome.Success && outcome.value != draft.path
            if (isMoved) emit(effect = VaultTreeEffect.EntryRenamed(from = draft.path, to = outcome.value))
            updateTree { reduceRenameOutcome(state = it, from = draft.path, outcome = outcome) }
            if (isMoved) updateLinks(notesBefore = notesBefore, from = draft.path, to = outcome.value)
        }
    }

    /** Selects the note a clicked link leads to; a created note joins the tree at once, before the watcher sees it. */
    private fun follow(
        intent: VaultTreeIntent.OnFollowLink
    ) {
        launchGuarded(onError = ::showUnexpectedError) {
            val outcome = followLink(
                vaultPath = vaultPath,
                notes = state.value.tree.notes.keys,
                source = intent.source,
                target = intent.target,
                kind = intent.kind
            )
            updateTree { reduceFollowedLink(state = it, outcome = outcome) }
        }
    }

    /** The editor's title renamed a note: the tree selects it at its new path and reports the links it updated. */
    private fun entryRenamed(
        intent: VaultTreeIntent.OnEntryRenamed
    ) {
        updateTree { reduceRenameOutcome(state = it, from = intent.from, outcome = Outcome.Success(value = intent.to)) }
        intent.links?.let { result -> reduce { reduceLinksUpdated(state = it, result = result) } }
    }

    /** Rewrites the links to [from] in the notes that had them, now that it is [to]; nothing when none link to it. */
    private suspend fun updateLinks(
        notesBefore: Set<RelativePath>,
        from: RelativePath,
        to: RelativePath
    ) {
        val result = updateLinksAfterRename(vaultPath = vaultPath, notesBefore = notesBefore, from = from, to = to)
            ?: return
        logger.i { LINKS_UPDATED_MESSAGE + result.updatedCount + NOTES_SKIPPED + result.skipped.size }
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
        private const val LINKS_UPDATED_MESSAGE = "Links updated after a rename: "
        private const val NOTES_IN = " notes in "
        private const val NOTES_SKIPPED = " notes, skipped: "
        private const val TREE_LOADED_MESSAGE = "Vault tree loaded: "
        private const val LOG_TAG = "VaultTreeViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the vault tree"
    }
}
