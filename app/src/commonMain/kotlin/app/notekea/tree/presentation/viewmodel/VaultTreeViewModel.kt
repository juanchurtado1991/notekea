package app.notekea.tree.presentation.viewmodel

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.links.UpdateLinksAfterRenameUseCase
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.BaseViewModel
import app.notekea.tree.domain.BookmarkStore
import app.notekea.tree.domain.CreateFolderUseCase
import app.notekea.tree.domain.FileRevealer
import app.notekea.tree.domain.FlattenTreeUseCase
import app.notekea.tree.domain.FollowLinkUseCase
import app.notekea.tree.domain.ObserveVaultTreeUseCase
import app.notekea.tree.domain.Trash
import app.notekea.tree.domain.TreePreferences
import app.notekea.tree.domain.TreePreferencesStore
import app.notekea.tree.presentation.model.BookmarkIntent
import app.notekea.tree.presentation.model.VaultTreeEffect
import app.notekea.tree.presentation.model.VaultTreeIntent
import app.notekea.tree.presentation.model.VaultTreeState
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.TimeSource

/**
 * Vault sidebar: shows the vault tree, kept up to date by the file watcher, and runs the actions on its entries. The
 * expanded folders and the sort are restored when the vault opens and saved on every change; the rows are flattened
 * again whenever they or the tree change. Bookmarks, daily notes, templates and renaming are parts of their own.
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
    fileRevealer: FileRevealer,
    updateLinksAfterRename: UpdateLinksAfterRenameUseCase,
    private val followLink: FollowLinkUseCase,
    private val treePreferencesStore: TreePreferencesStore,
    fileOpener: FileOpener,
    days: DailyNoteTools,
    bookmarkStore: BookmarkStore
) : BaseViewModel<VaultTreeState, VaultTreeIntent, VaultTreeEffect>(
    initialState = VaultTreeState(
        vaultPath = vaultPath,
        platformConventions = platformConventions
    )
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val host = object : TreeHost {
        override val state: StateFlow<VaultTreeState> get() = this@VaultTreeViewModel.state

        override fun reduce(transform: (VaultTreeState) -> VaultTreeState) =
            this@VaultTreeViewModel.reduce(
                transform = transform
            )

        override fun updateTree(transform: (VaultTreeState) -> VaultTreeState) =
            this@VaultTreeViewModel.updateTree(
                transform = transform
            )

        override fun emit(effect: VaultTreeEffect) = this@VaultTreeViewModel.emit(effect = effect)

        override fun launch(block: suspend () -> Unit): Job = launchGuarded(
            onError = ::showUnexpectedError,
            block = block
        )

        override fun open(path: RelativePath?) = this@VaultTreeViewModel.open(path = path)

        override fun reveal(path: RelativePath) = this@VaultTreeViewModel.reveal(path = path)
    }
    private val files = TreeFiles(host, vaultPath, fileOpener, fileRevealer)
    private val bookmarks = TreeBookmarks(
        host = host,
        vaultPath = vaultPath,
        bookmarkStore = bookmarkStore
    )
    private val dailyNotes = TreeDays(
        host = host,
        vaultPath = vaultPath,
        days = days
    )
    private val templates = TreeTemplates(
        host = host,
        vaultPath = vaultPath,
        newNotes = newNotes
    )
    private val renaming =
        TreeRenaming(host, vaultPath, renameEntry, updateLinksAfterRename, bookmarks, templates)

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            val preferences = treePreferencesStore.read(vaultPath = vaultPath)

            updateTree {
                restorePreferences(
                    state = it,
                    preferences = preferences
                )
            }

            dailyNotes.loadSettings()
            templates.loadSettings()
            bookmarks.load()
            observe()
        }
    }

    override fun handleIntent(intent: VaultTreeIntent) {
        when (intent) {
            is VaultTreeIntent.OnChevronClick -> updateTree {
                reduceToggleFolder(
                    state = it,
                    folder = intent.path
                )
            }

            VaultTreeIntent.OnCollapseSelected -> updateTree(transform = ::reduceCollapseSelected)

            VaultTreeIntent.OnDeleteCancel -> reduce { it.copy(pendingDelete = null) }

            VaultTreeIntent.OnDeleteConfirm -> delete()

            is VaultTreeIntent.OnEntryRenamed -> renaming.onRenamedElsewhere(intent = intent)

            is VaultTreeIntent.OnDeleteRequest -> reduce {
                it.copy(
                    pendingDelete =
                    intent.path ?: it.selectedPath
                )
            }

            VaultTreeIntent.OnErrorDismiss -> reduce {
                it.copy(
                    actionError = null,
                    hasUnexpectedError = false
                )
            }

            VaultTreeIntent.OnExpandSelected -> updateTree(transform = ::reduceExpandSelected)

            VaultTreeIntent.OnExpandAll -> updateTree { it.copy(expandedFolders = it.tree.folders) }

            VaultTreeIntent.OnCollapseAll -> updateTree { it.copy(expandedFolders = emptySet()) }

            is VaultTreeIntent.OnFollowLink -> follow(intent = intent)

            is VaultTreeIntent.OnNewFolderClick -> create(
                baseName = intent.baseName,
                isFolder = true
            )

            is VaultTreeIntent.OnNewNoteClick -> create(
                baseName = intent.baseName,
                isFolder = false
            )

            VaultTreeIntent.OnNewNoteFromTemplateClick -> templates.choose()

            is VaultTreeIntent.OnNoteFromTemplate -> templates.create(intent = intent)

            VaultTreeIntent.OnNoticeDismiss -> reduce { it.copy(notice = null) }

            VaultTreeIntent.OnUnexpectedError, is VaultTreeIntent.OnUnsavedKept -> tellFromOutside(
                intent = intent
            )

            VaultTreeIntent.OnRenameCancel -> renaming.cancel()

            VaultTreeIntent.OnRenameConfirm -> renaming.confirm()

            is VaultTreeIntent.OnRenameRequest -> renaming.start(path = intent.path)

            is VaultTreeIntent.OnRenameTextChange -> renaming.changeText(text = intent.text)

            VaultTreeIntent.OnRetryClick -> retry()

            is VaultTreeIntent.OnRevealRequest -> files.showInFileManager(path = intent.path)

            is VaultTreeIntent.OnRevealNote -> reveal(path = intent.path)

            is VaultTreeIntent.OnRevealInTree -> revealInTree(path = intent.path)

            is VaultTreeIntent.OnRowClick -> clickRow(
                path = intent.path,
                inBackground = intent.inBackground
            )

            VaultTreeIntent.OnOpenSelected -> openSelected()

            is VaultTreeIntent.OnRowSelect -> reduce { it.copy(selectedPath = intent.path) }

            VaultTreeIntent.OnSelectionClear -> reduce { it.copy(selectedPath = null) }

            VaultTreeIntent.OnSelectNext, VaultTreeIntent.OnSelectPrevious -> moveSelection(
                intent = intent
            )

            is VaultTreeIntent.OnSidebarResize -> resizeSidebar(widthDp = intent.widthDp)

            VaultTreeIntent.OnSidebarToggle -> updateTree {
                it.copy(isSidebarCollapsed = !it.isSidebarCollapsed)
            }

            is VaultTreeIntent.OnSortChange -> updateTree { it.copy(sort = intent.sort) }

            is VaultTreeIntent.OnFileOpen -> files.openWithApp(path = intent.path)

            is BookmarkIntent -> bookmarks.onIntent(intent = intent)

            VaultTreeIntent.OnTodayClick, is VaultTreeIntent.OnDayStep -> dailyNotes.onIntent(
                intent = intent
            )
        }

        dailyNotes.update()
    }

    /** The editors and the tabs have no banner of their own: the sidebar's says what happened to them. */
    private fun tellFromOutside(intent: VaultTreeIntent) {
        reduce {
            if (intent is VaultTreeIntent.OnUnsavedKept) {
                it.copy(notice = unsavedNotice(intent = intent))
            } else {
                it.copy(hasUnexpectedError = true)
            }
        }
    }

    private fun moveSelection(intent: VaultTreeIntent) {
        val step = if (intent == VaultTreeIntent.OnSelectNext) 1 else -1

        reduce {
            reduceSelectionMove(
                state = it,
                step = step
            )
        }
    }

    private fun openSelected() {
        state
            .value
            .selectedPath
            ?.let {
                clickRow(
                    path = it,
                    inBackground = false
                )
            }
    }

    /**
     * A note, file or folder clicked (or chosen with Enter) opens in a tab; a folder also opens or closes in the tree.
     */
    private fun clickRow(
        path: RelativePath,
        inBackground: Boolean
    ) {
        updateTree {
            reduceRowClick(
                state = it,
                path = path
            )
        }

        open(
            path = path,
            inBackground = inBackground
        )
    }

    /** An entry chosen outside the tree (the quick switcher) is selected with its folders open, and opened in a tab. */
    private fun reveal(path: RelativePath) {
        updateTree {
            reduceReveal(
                state = it,
                path = path
            )
        }

        open(path = path)
    }

    /** Asks the tabs to open [path]; [inBackground] keeps the current tab shown (`Mod+click`, middle click). */
    private fun open(
        path: RelativePath?,
        inBackground: Boolean = false
    ) {
        path?.let {
            emit(
                effect = VaultTreeEffect.OpenEntry(
                    path = it,
                    inBackground = inBackground
                )
            )
        }
    }

    /**
     * A folder or file named outside the tree (the status bar, the folder above a title) is shown in Files, opening the
     * sidebar if it was hidden; a note chosen in the quick switcher goes to [reveal].
     */
    private fun revealInTree(path: RelativePath) {
        updateTree {
            reduceReveal(
                state = it,
                path = path
            ).copy(isSidebarCollapsed = false)
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
        if (state.value.isSidebarCollapsed) {
            updateTree { it.copy(isSidebarCollapsed = false) }
        }

        launchGuarded(onError = ::showUnexpectedError) {
            val folder = targetFolder(state = state.value)

            val outcome = if (isFolder) {
                createFolder(
                    vaultPath = vaultPath,
                    folder = folder,
                    baseName = baseName
                )
            } else {
                newNotes.createNote(
                    vaultPath = vaultPath,
                    folder = folder,
                    baseName = baseName
                )
            }

            updateTree {
                reduceCreated(
                    state = it,
                    outcome = outcome,
                    isFolder = isFolder
                )
            }

            if (!isFolder) open(path = (outcome as? Outcome.Success)?.value)
        }
    }

    private fun delete() {
        val path = state.value.pendingDelete ?: return

        launchGuarded(onError = ::showUnexpectedError) {
            val outcome = trash.moveToTrash(
                vaultPath = vaultPath,
                path = path
            )

            updateTree {
                reduceDeleted(
                    state = it,
                    path = path,
                    outcome = outcome
                )
            }

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
                    reduce {
                        reduceLoadFailed(
                            state = it,
                            error = outcome.error
                        )
                    }
                }

                is Outcome.Success -> {
                    if (isFirstTree) {
                        logger.i {
                            TREE_LOADED_MESSAGE + outcome.value.notes.size + NOTES_IN +
                                start.elapsedNow()
                        }
                    }

                    isFirstTree = false

                    updateTree {
                        reduceTreeLoaded(
                            state = it,
                            tree = renaming.withPending(tree = outcome.value)
                        )
                    }
                }
            }
        }
    }

    /** Selects the note a clicked link leads to; a created note joins the tree at once, before the watcher sees it. */
    private fun follow(intent: VaultTreeIntent.OnFollowLink) {
        launchGuarded(onError = ::showUnexpectedError) {
            val outcome = followLink(
                vaultPath = vaultPath,
                notes = state.value.tree.notes.keys,
                source = intent.source,
                target = intent.target,
                kind = intent.kind
            )

            updateTree {
                reduceFollowedLink(
                    state = it,
                    outcome = outcome
                )
            }

            open(
                path = (outcome as? Outcome.Success)?.value?.path,
                inBackground = intent.inBackground
            )
        }
    }

    private fun resizeSidebar(widthDp: Int) {
        updateTree {
            it.copy(sidebarWidthDp = TreePreferences.clampSidebarWidth(widthDp = widthDp))
        }
    }

    private fun retry() {
        if (state.value.loadError == null) return

        reduce {
            it.copy(
                isLoading = it.tree.notes.isEmpty() && it.tree.folders.isEmpty(),
                loadError = null
            )
        }

        launchGuarded(onError = ::showUnexpectedError) { observe() }
    }

    private fun showUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }

        reduce {
            it.copy(
                isLoading = false,
                hasUnexpectedError = true
            )
        }
    }

    /** Applies [transform], flattens the rows again and saves the expanded folders and sort if they changed. */
    private fun updateTree(transform: (VaultTreeState) -> VaultTreeState) {
        val before = state.value

        reduce { current ->
            val next = transform(current)

            // A change of selection, rename or dialog leaves the rows as they are: flattening walks the whole vault.
            val isLayoutSame =
                next.tree === current.tree &&
                    next.expandedFolders == current.expandedFolders &&
                    next.sort == current.sort

            if (isLayoutSame) {
                next
            } else {
                next.copy(
                    rows = flattenTree(
                        tree = next.tree,
                        expandedFolders = next.expandedFolders,
                        sort = next.sort
                    )
                )
            }
        }

        val after = state.value
        dailyNotes.update()
        val preferences = preferencesOf(state = after)

        val hasNewPreferences = preferences != preferencesOf(state = before) && !after.isLoading

        if (hasNewPreferences) {
            launchGuarded(onError = ::showUnexpectedError) {
                treePreferencesStore.write(
                    vaultPath = vaultPath,
                    preferences = preferences
                )
            }
        }
    }

    companion object {
        private const val NOTES_IN = " notes in "
        private const val TREE_LOADED_MESSAGE = "Vault tree loaded: "
        private const val LOG_TAG = "VaultTreeViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the vault tree"
    }
}
