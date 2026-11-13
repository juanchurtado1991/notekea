package app.notekea.shell.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.sidepanel.SidePanelMode
import app.notekea.core.notes.domain.sidepanel.SidePanelPreferences
import app.notekea.core.notes.domain.sidepanel.SidePanelSection
import app.notekea.core.presentation.BaseViewModel
import app.notekea.core.presentation.designsystem.images.decodedImage
import app.notekea.core.presentation.designsystem.images.scaledToCover
import app.notekea.shell.domain.Workspace
import app.notekea.tree.presentation.viewmodel.TreeFilePreviews
import co.touchlab.kermit.Logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The editor's tabs and panes of the vault at [vaultPath]. Every opened note, file or folder gets a tab, or goes to the
 * one it has; the tabs and the split are saved on every change and restored when the vault opens again, without the
 * tabs of files that are gone. It also reads the files a tab shows and the folder grid's pictures.
 */
class WorkspaceViewModel(
    private val vaultPath: String,
    private val tools: WorkspaceTools
) : BaseViewModel<WorkspaceState, WorkspaceIntent, WorkspaceEffect>(
    initialState = WorkspaceState()
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val filePreviews = TreeFilePreviews(
        vaultPath = vaultPath,
        attachmentStore = tools.attachmentStore,
        dispatchers = tools.dispatchers
    )

    // One save at a time, each writing the latest state, so a slow save never lands after a newer one.
    private val savingPanel = Mutex()
    private val savingTabs = Mutex()

    // The tabs of the last run, until the vault's files are known ([treeLoaded]).
    private var saved: Workspace? = null
    private var treeLoaded = false

    // The file each cover was read from, so a picture changed on disk is read again.
    private val coverSources = mutableMapOf<RelativePath, NoteFile?>()

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            val panel = tools.getSidePanel()

            reduce { it.copy(sidePanel = panel) }

            saved = tools.getWorkspace(vaultPath = vaultPath)
            restoreIfReady()
        }
    }

    override fun handleIntent(intent: WorkspaceIntent) {
        when (intent) {
            is WorkspaceIntent.OnTreeChange -> changeTree(intent = intent)

            is WorkspaceIntent.OnOpen -> change {
                it.open(
                    path = intent.path,
                    inBackground = intent.inBackground
                )
            }

            is WorkspaceIntent.OnTabSelect -> change {
                it.select(
                    pane = intent.pane,
                    tab = intent.tab
                )
            }

            is WorkspaceIntent.OnTabClose -> change {
                it.close(
                    pane = intent.pane,
                    tab = intent.tab
                )
            }

            is WorkspaceIntent.OnCloseOthers -> change {
                it.closeOthers(
                    pane = intent.pane,
                    tab = intent.tab
                )
            }

            WorkspaceIntent.OnCloseActive -> change {
                it.close(
                    pane = it.activePane,
                    tab = it.panes[it.activePane].active
                )
            }

            is WorkspaceIntent.OnCycle -> change { it.cycle(step = intent.step) }

            is WorkspaceIntent.OnJump -> change { it.jump(number = intent.number) }

            WorkspaceIntent.OnSplitRight -> change { it.splitRight() }

            is WorkspaceIntent.OnOpenBeside -> change { it.openBeside(path = intent.path) }

            is WorkspaceIntent.OnTabMove -> change {
                it.move(
                    path = intent.path,
                    toPane = intent.toPane,
                    toIndex = intent.toIndex
                )
            }

            is WorkspaceIntent.OnPaneFocus -> change { it.focus(pane = intent.pane) }

            is WorkspaceIntent.OnRenamed -> change {
                it.renamed(
                    from = intent.from,
                    to = intent.to
                )
            }

            is WorkspaceIntent.OnDeleted -> change { it.without(path = intent.path) }

            is WorkspaceIntent.OnCoverRequest -> readCover(
                path = intent.path,
                size = intent.size
            )

            is WorkspaceIntent.SidePanel -> onSidePanelIntent(intent = intent)
        }
    }

    private fun onSidePanelIntent(intent: WorkspaceIntent.SidePanel) {
        when (intent) {
            WorkspaceIntent.OnSidePanelToggle -> updateSidePanel {
                it.copy(isVisible = !it.isVisible)
            }

            is WorkspaceIntent.OnSidePanelResize -> updateSidePanel {
                it.copy(widthDp = intent.widthDp)
            }

            WorkspaceIntent.OnOutlineFocusRequest -> {
                val panel = state.value.sidePanel

                val shown = panel.copy(
                    isVisible = true,
                    mode = SidePanelMode.INSPECTOR,
                    foldedSections = panel.foldedSections - SidePanelSection.OUTLINE
                )

                if (shown != panel) updateSidePanel { shown }
                emit(effect = WorkspaceEffect.FocusOutline)
            }

            WorkspaceIntent.OnChatFocusRequest -> {
                val panel = state.value.sidePanel

                val shown = panel.copy(
                    isVisible = true,
                    mode = SidePanelMode.CHAT
                )

                if (shown != panel) updateSidePanel { shown }
                emit(effect = WorkspaceEffect.FocusChat)
            }

            is WorkspaceIntent.OnSidePanelModeSelect -> updateSidePanel {
                it.copy(mode = intent.mode)
            }

            is WorkspaceIntent.OnConnectionsTabSelect -> updateSidePanel {
                it.copy(connections = intent.tab)
            }

            is WorkspaceIntent.OnSidePanelSectionToggle -> updateSidePanel {
                val folded = it.foldedSections

                val foldedSections = if (intent.section in folded) {
                    folded - intent.section
                } else {
                    folded + intent.section
                }

                it.copy(foldedSections = foldedSections)
            }
        }
    }

    /** Applies [transform] to the tabs, saves them once they are restored, and reads the files they now show. */
    private fun change(transform: (Workspace) -> Workspace) {
        val before = state.value.workspace

        reduce { it.copy(workspace = transform(it.workspace)) }

        val hasNewTabs = state.value.workspace != before && state.value.isRestored
        if (hasNewTabs) saveTabs()
        readPreviews()
    }

    private fun changeTree(intent: WorkspaceIntent.OnTreeChange) {
        reduce { it.copy(tree = intent.tree) }

        val stale = coverSources.filter { (path, source) ->
            sourceOf(path = path) != source
        }.keys

        if (stale.isNotEmpty()) {
            stale.forEach { coverSources.remove(key = it) }
            reduce { it.copy(covers = it.covers - stale) }
        }

        // The tree remembered from the last run can still list files deleted while Notekea was closed (1.11-10), so
        // the tabs come back against the scanned one.
        val isScanned = intent.isLoaded && !intent.tree.isFromCache

        if (isScanned) {
            treeLoaded = true
            restoreIfReady()
        }

        readPreviews()
    }

    /** Once the vault's files are known, the last run's tabs come back, those of files that are gone closed. */
    private fun restoreIfReady() {
        val workspace = saved ?: return
        val current = state.value
        if (current.isRestored || !treeLoaded) return
        saved = null

        val existing =
            current.tree.notes.keys + current.tree.attachments.keys + current.tree.folders

        val opened = current
            .workspace
            .panes
            .flatMap { it.tabs }

        val restored = opened
            .fold(initial = workspace.keepingOnly(existing = existing)) { tabs, path ->
                tabs.open(path)
            }

        reduce {
            it.copy(
                workspace = restored,
                isRestored = true
            )
        }

        saveTabs()
        readPreviews()
    }

    private fun saveTabs() {
        launchGuarded(onError = ::showUnexpectedError) {
            savingTabs.withLock {
                tools.workspaceStore.write(
                    vaultPath = vaultPath,
                    workspace = state.value.workspace
                )
            }
        }
    }

    /**
     * Reads the file shown by each pane's tab, when it is a file and was not read yet or changed on disk, and lets go
     * of the files no tab holds any more: a full-size image can take tens of megabytes.
     */
    private fun readPreviews() {
        val open = state
            .value
            .workspace
            .panes
            .flatMapTo(destination = mutableSetOf()) { it.tabs }

        if (state.value.previews.keys.any { it !in open }) {
            reduce {
                it.copy(
                    previews = it.previews.filterKeys { path ->
                        path in open
                    }
                )
            }
        }

        val current = state.value

        current
            .workspace
            .panes
            .mapNotNull { it.activePath }
            .forEach { path ->
                val file = current.tree.attachments[path] ?: return@forEach
                if (current.previews[path]?.file == file) return@forEach

                launchGuarded(onError = ::showUnexpectedError) {
                    val preview = filePreviews.of(file = file)

                    reduce { it.copy(previews = it.previews + (path to preview)) }
                }
            }
    }

    /**
     * Reads the picture of a folder grid's card, the image itself or the first image a note embeds, at the [size] the
     * card needs; only the [MAX_COVERS] newest stay, the others are read again if their card comes back into view.
     */
    private fun readCover(
        path: RelativePath,
        size: IntSize
    ) {
        if (path in coverSources) return
        coverSources[path] = sourceOf(path = path)

        launchGuarded(onError = ::logCoverError) {
            val tree = state.value.tree

            val picture = if (path.isMarkdown) {
                tools.getNoteCover(
                    vaultPath = vaultPath,
                    note = path,
                    files = tree.attachments.keys
                )
            } else {
                path.takeIf {
                    AttachmentKind.of(name = it.name) == AttachmentKind.IMAGE
                }
            }

            val image = picture?.let {
                imageOf(
                    path = it,
                    size = size
                )
            }

            val held = state.value.covers.keys - path + path

            val dropped = held.take(
                n = (held.size - MAX_COVERS).coerceAtLeast(minimumValue = 0)
            ).toSet()

            dropped.forEach { coverSources.remove(key = it) }

            reduce { it.copy(covers = it.covers - path + (path to image) - dropped) }
        }
    }

    private suspend fun imageOf(
        path: RelativePath,
        size: IntSize
    ): ImageBitmap? {
        val bytes = (
            tools.attachmentStore.read(
                vaultPath = vaultPath,
                path = path
            ) as? Outcome.Success
            )?.value
            ?: return null

        val image = decodedImage(
            bytes = bytes,
            dispatcher = tools.dispatchers.default
        )

        return image?.scaledToCover(
            width = size.width,
            height = size.height
        )
    }

    private fun sourceOf(path: RelativePath): NoteFile? = state
        .value
        .tree
        .let {
            it.notes[path]?.file ?: it.attachments[path]
        }

    private fun updateSidePanel(change: (SidePanelPreferences) -> SidePanelPreferences) {
        reduce { it.copy(sidePanel = change(it.sidePanel)) }

        launchGuarded(onError = ::showUnexpectedError) {
            savingPanel.withLock { tools.saveSidePanel(preferences = state.value.sidePanel) }
        }
    }

    private fun showUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        emit(effect = WorkspaceEffect.UnexpectedError)
    }

    /** A card without its picture shows its icon, so this is worth a line in the log and nothing more. */
    private fun logCoverError(throwable: Throwable) {
        logger.w(throwable = throwable) { COVER_ERROR_MESSAGE }
    }

    companion object {
        /**
         * The most folder grid cards whose pictures are kept: more than a screen of cards, so scrolling back is quick.
         */
        const val MAX_COVERS = 60
        private const val LOG_TAG = "WorkspaceViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the editor's tabs"
        private const val COVER_ERROR_MESSAGE = "A card's picture could not be read"
    }
}
