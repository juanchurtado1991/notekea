package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.BookmarkStore
import app.notekea.notes.domain.TreePreferencesStore
import app.notekea.notes.domain.FileRevealer
import app.notekea.notes.domain.Trash
import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.FollowLinkUseCase
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.TimeSource

/**
 * Vault sidebar (`ROADMAP.md` 1.4): shows the vault tree, kept up to date by the file watcher, and runs the actions
 * on its entries. The expanded folders and the sort are restored when the vault opens and saved on every change. The
 * rows are flattened again whenever the tree, the expanded folders or the sort change. Renaming can be confirmed twice
 * at once (Enter and the field losing focus); only the first confirmation runs. After a rename, the notes that link to
 * the entry are updated in the background, as Obsidian does by default, so no link breaks; a notice says how many
 * notes changed. A choice to keep old links ("Update links when renaming") belongs to Settings (`ROADMAP.md` 5.2).
 * A file that is not a note (an image, a PDF) opens with its app (`ROADMAP.md` 1.8). "Today" opens today's daily note, creating it first if needed, and a selected daily note knows the
 * daily notes before and after it, to step through them (`ROADMAP.md` 1.10).
 */
class VaultTreeViewModel(
    private val vaultPath: String,
    platformConventions: PlatformConventions,
    private val observeVaultTree: ObserveVaultTreeUseCase,
    private val flattenTree: FlattenTreeUseCase,
    private val newNotes: NewNoteTools,
    private val createFolder: CreateFolderUseCase,
    renameEntry: RenameEntryUseCase,
    private val trash: Trash,
    private val fileRevealer: FileRevealer,
    updateLinksAfterRename: UpdateLinksAfterRenameUseCase,
    private val followLink: FollowLinkUseCase,
    private val treePreferencesStore: TreePreferencesStore,
    private val fileOpener: FileOpener,
    days: DailyNoteTools,
    bookmarkStore: BookmarkStore
) : BaseViewModel<VaultTreeState, VaultTreeIntent, VaultTreeEffect>(
    initialState = VaultTreeState(vaultPath = vaultPath, platformConventions = platformConventions)
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val host = object : TreeHost {
        override val state: StateFlow<VaultTreeState> get() = this@VaultTreeViewModel.state

        override fun reduce(
            transform: (VaultTreeState) -> VaultTreeState
        ) = this@VaultTreeViewModel.reduce(transform = transform)

        override fun updateTree(
            transform: (VaultTreeState) -> VaultTreeState
        ) = this@VaultTreeViewModel.updateTree(transform = transform)

        override fun emit(
            effect: VaultTreeEffect
        ) = this@VaultTreeViewModel.emit(effect = effect)

        override fun launch(
            block: suspend () -> Unit
        ): Job = launchGuarded(onError = ::showUnexpectedError, block = block)

        override fun open(
            path: RelativePath?
        ) = this@VaultTreeViewModel.open(path = path)

        override fun reveal(
            path: RelativePath
        ) = this@VaultTreeViewModel.reveal(path = path)
    }
    private val bookmarks = TreeBookmarks(host = host, vaultPath = vaultPath, bookmarkStore = bookmarkStore)
    private val dailyNotes = TreeDays(host = host, vaultPath = vaultPath, days = days)
    private val templates = TreeTemplates(host = host, vaultPath = vaultPath, newNotes = newNotes)
    private val renaming = TreeRenaming(host, vaultPath, renameEntry, updateLinksAfterRename, bookmarks, templates)

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            val preferences = treePreferencesStore.read(vaultPath = vaultPath)
            updateTree { restorePreferences(state = it, preferences = preferences) }
            dailyNotes.loadSettings()
            bookmarks.load()
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
            is VaultTreeIntent.OnEntryRenamed -> renaming.onRenamedElsewhere(intent = intent)
            is VaultTreeIntent.OnDeleteRequest -> reduce { it.copy(pendingDelete = intent.path ?: it.selectedPath) }
            VaultTreeIntent.OnErrorDismiss -> reduce { it.copy(actionError = null, hasUnexpectedError = false) }
            VaultTreeIntent.OnExpandSelected -> updateTree(transform = ::reduceExpandSelected)
            VaultTreeIntent.OnExpandAll -> updateTree { it.copy(expandedFolders = it.tree.folders) }
            VaultTreeIntent.OnCollapseAll -> updateTree { it.copy(expandedFolders = emptySet()) }
            is VaultTreeIntent.OnFollowLink -> follow(intent = intent)
            is VaultTreeIntent.OnNewFolderClick -> create(baseName = intent.baseName, isFolder = true)
            is VaultTreeIntent.OnNewNoteClick -> create(baseName = intent.baseName, isFolder = false)
            VaultTreeIntent.OnNewNoteFromTemplateClick -> templates.choose()
            is VaultTreeIntent.OnNoteFromTemplate -> templates.create(intent = intent)
            VaultTreeIntent.OnNoticeDismiss -> reduce { it.copy(notice = null) }
            VaultTreeIntent.OnRenameCancel -> renaming.cancel()
            VaultTreeIntent.OnRenameConfirm -> renaming.confirm()
            is VaultTreeIntent.OnRenameRequest -> renaming.start(path = intent.path)
            is VaultTreeIntent.OnRenameTextChange -> renaming.changeText(text = intent.text)
            VaultTreeIntent.OnRetryClick -> retry()
            is VaultTreeIntent.OnRevealRequest -> reveal(path = intent.path)
            is VaultTreeIntent.OnRevealNote, is VaultTreeIntent.OnRevealInTree -> revealFromOutside(intent = intent)
            is VaultTreeIntent.OnRowClick -> clickRow(path = intent.path, inBackground = intent.inBackground)
            VaultTreeIntent.OnOpenSelected -> openSelected()
            is VaultTreeIntent.OnRowSelect -> reduce { it.copy(selectedPath = intent.path) }
            VaultTreeIntent.OnSelectionClear -> reduce { it.copy(selectedPath = null) }
            VaultTreeIntent.OnSelectNext -> reduce { reduceSelectionMove(state = it, step = 1) }
            VaultTreeIntent.OnSelectPrevious -> reduce { reduceSelectionMove(state = it, step = -1) }
            is VaultTreeIntent.OnSidebarResize -> resizeSidebar(widthDp = intent.widthDp)
            VaultTreeIntent.OnSidebarToggle -> updateTree { it.copy(isSidebarCollapsed = !it.isSidebarCollapsed) }
            is VaultTreeIntent.OnSortChange -> updateTree { it.copy(sort = intent.sort) }
            is VaultTreeIntent.OnFileOpen -> openFile(path = intent.path)
            is BookmarkIntent -> bookmarks.onIntent(intent = intent)
            VaultTreeIntent.OnTodayClick, is VaultTreeIntent.OnDayStep -> dailyNotes.onIntent(intent = intent)
        }
        dailyNotes.update()
    }

    private fun openSelected() {
        state.value.selectedPath?.let { clickRow(path = it, inBackground = false) }
    }

    /** A note, file or folder clicked (or chosen with Enter) opens in a tab; a folder also opens or closes in the tree. */
    private fun clickRow(
        path: RelativePath,
        inBackground: Boolean
    ) {
        updateTree { reduceRowClick(state = it, path = path) }
        open(path = path, inBackground = inBackground)
    }

    /** An entry chosen outside the tree (the quick switcher) is selected with its folders open, and opened in a tab. */
    private fun reveal(
        path: RelativePath
    ) {
        updateTree { reduceReveal(state = it, path = path) }
        open(path = path)
    }

    /** Asks the tabs to open [path]; [inBackground] keeps the current tab shown (`Mod+click`, middle click). */
    private fun open(
        path: RelativePath?,
        inBackground: Boolean = false
    ) {
        path?.let { emit(effect = VaultTreeEffect.OpenEntry(path = it, inBackground = inBackground)) }
    }

    /**
     * An entry named outside the tree: a note chosen in the quick switcher is selected and opened; a folder or file of
     * a path (the status bar, the folder above a title) is shown in Files, opening the sidebar if it was hidden.
     */
    private fun revealFromOutside(
        intent: VaultTreeIntent
    ) {
        when (intent) {
            is VaultTreeIntent.OnRevealNote -> reveal(path = intent.path)

            is VaultTreeIntent.OnRevealInTree ->
                updateTree { reduceReveal(state = it, path = intent.path).copy(isSidebarCollapsed = false) }

            else -> Unit
        }
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

    /**
     * Creates a note or folder in the selected folder; a hidden sidebar opens first, so the new entry is seen where it
     * went, with its name ready to type.
     */
    private fun create(
        baseName: String,
        isFolder: Boolean
    ) {
        if (state.value.isSidebarCollapsed) updateTree { it.copy(isSidebarCollapsed = false) }
        launchGuarded(onError = ::showUnexpectedError) {
            val folder = targetFolder(state = state.value)
            val outcome = if (isFolder) {
                createFolder(vaultPath = vaultPath, folder = folder, baseName = baseName)
            } else {
                newNotes.createNote(vaultPath = vaultPath, folder = folder, baseName = baseName)
            }
            updateTree { reduceCreated(state = it, outcome = outcome, isFolder = isFolder) }
            if (!isFolder) open(path = (outcome as? Outcome.Success)?.value)
        }
    }

    private fun delete() {
        val path = state.value.pendingDelete ?: return
        launchGuarded(onError = ::showUnexpectedError) {
            val outcome = trash.moveToTrash(vaultPath = vaultPath, path = path)
            updateTree { reduceDeleted(state = it, path = path, outcome = outcome) }
            if (outcome is Outcome.Success) {
                bookmarks.change { it.deleted(path = path) }
                emit(effect = VaultTreeEffect.EntryDeleted(path = path))
            }
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
                }
            }
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
            open(path = (outcome as? Outcome.Success)?.value?.path, inBackground = intent.inBackground)
        }
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
            val outcome = fileRevealer.reveal(vaultPath = vaultPath, path = target)
            if (outcome is Outcome.Failure) reduce { it.copy(actionError = outcome.error) }
        }
    }

    private fun showUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false, hasUnexpectedError = true) }
    }

    /** Applies [transform], flattens the rows again and saves the expanded folders and sort if they changed. */
    private fun updateTree(
        transform: (VaultTreeState) -> VaultTreeState
    ) {
        val before = state.value
        reduce { current ->
            val next = transform(current)
            // A change of selection, rename or dialog leaves the rows as they are: flattening walks the whole vault.
            val isLayoutSame = next.tree === current.tree && next.expandedFolders == current.expandedFolders &&
                next.sort == current.sort
            if (isLayoutSame) {
                next
            } else {
                next.copy(
                    rows = flattenTree(tree = next.tree, expandedFolders = next.expandedFolders, sort = next.sort)
                )
            }
        }
        val after = state.value
        dailyNotes.update()
        val preferences = preferencesOf(state = after)
        if (preferences != preferencesOf(state = before) && !after.isLoading) {
            launchGuarded(
                onError = ::showUnexpectedError
            ) { treePreferencesStore.write(vaultPath = vaultPath, preferences = preferences) }
        }
    }

    companion object {
        private const val NOTES_IN = " notes in "
        private const val TREE_LOADED_MESSAGE = "Vault tree loaded: "
        private const val LOG_TAG = "VaultTreeViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the vault tree"
    }
}
