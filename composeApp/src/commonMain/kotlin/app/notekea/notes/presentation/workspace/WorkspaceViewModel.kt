package app.notekea.notes.presentation.workspace

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.BaseViewModel
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelTab
import app.notekea.notes.domain.Workspace
import app.notekea.notes.presentation.tree.TreeFilePreviews
import co.touchlab.kermit.Logger
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * The editor's tabs and panes of the vault at [vaultPath] (`ROADMAP.md` 1.11). Every opened note, file or folder gets a
 * tab, or goes to its tab when it has one; the tabs and the split are saved on every change and restored when the vault
 * opens again, once its files are known, without the tabs of files that are gone. It reads the files shown in a tab
 * and the pictures of a folder grid's cards, and keeps how the panel on the right is left (`ROADMAP.md` 1.9), which is
 * one for both panes.
 */
class WorkspaceViewModel(
    private val vaultPath: String,
    private val tools: WorkspaceTools
) : BaseViewModel<WorkspaceState, WorkspaceIntent, WorkspaceEffect>(initialState = WorkspaceState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val filePreviews = TreeFilePreviews(
        vaultPath = vaultPath,
        attachmentStore = tools.attachmentStore,
        dispatchers = tools.dispatchers
    )

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

    override fun onIntent(
        intent: WorkspaceIntent
    ) {
        when (intent) {
            is WorkspaceIntent.OnTreeChange -> changeTree(intent = intent)

            is WorkspaceIntent.OnOpen -> change { it.open(path = intent.path, inBackground = intent.inBackground) }

            is WorkspaceIntent.OnTabSelect -> change { it.select(pane = intent.pane, tab = intent.tab) }

            is WorkspaceIntent.OnTabClose -> change { it.close(pane = intent.pane, tab = intent.tab) }

            is WorkspaceIntent.OnCloseOthers -> change { it.closeOthers(pane = intent.pane, tab = intent.tab) }

            WorkspaceIntent.OnCloseActive -> change {
                it.close(pane = it.activePane, tab = it.panes[it.activePane].active)
            }

            is WorkspaceIntent.OnCycle -> change { it.cycle(step = intent.step) }

            is WorkspaceIntent.OnJump -> change { it.jump(number = intent.number) }

            WorkspaceIntent.OnSplitRight -> change { it.splitRight() }

            is WorkspaceIntent.OnTabMove -> change {
                it.move(path = intent.path, toPane = intent.toPane, toIndex = intent.toIndex)
            }

            is WorkspaceIntent.OnPaneFocus -> change { it.focus(pane = intent.pane) }

            is WorkspaceIntent.OnRenamed -> change { it.renamed(from = intent.from, to = intent.to) }

            is WorkspaceIntent.OnDeleted -> change { it.without(path = intent.path) }

            is WorkspaceIntent.OnCoverRequest -> readCover(path = intent.path)

            else -> onSidePanelIntent(intent = intent)
        }
    }

    private fun onSidePanelIntent(
        intent: WorkspaceIntent
    ) {
        when (intent) {
            WorkspaceIntent.OnSidePanelToggle -> updateSidePanel { it.copy(isVisible = !it.isVisible) }

            is WorkspaceIntent.OnSidePanelResize -> updateSidePanel { it.copy(widthDp = intent.widthDp) }

            WorkspaceIntent.OnOutlineFocusRequest -> {
                val panel = state.value.sidePanel
                if (!panel.isVisible || panel.tab != SidePanelTab.OUTLINE) {
                    updateSidePanel { it.copy(isVisible = true, tab = SidePanelTab.OUTLINE) }
                }
                emit(effect = WorkspaceEffect.FocusOutline)
            }

            is WorkspaceIntent.OnSidePanelTabSelect -> updateSidePanel { it.copy(tab = intent.tab, isVisible = true) }

            WorkspaceIntent.OnPropertiesInNoteToggle -> updateSidePanel { panel ->
                if (panel.arePropertiesInNote) {
                    panel.copy(arePropertiesInNote = false, isVisible = true, tab = SidePanelTab.PROPERTIES)
                } else {
                    panel.copy(arePropertiesInNote = true)
                }
            }

            else -> Unit
        }
    }

    /** Applies [transform] to the tabs, saves them once they are restored, and reads the files they now show. */
    private fun change(
        transform: (Workspace) -> Workspace
    ) {
        val before = state.value.workspace
        reduce { it.copy(workspace = transform(it.workspace)) }
        val after = state.value.workspace
        if (after != before && state.value.isRestored) {
            launchGuarded(onError = ::showUnexpectedError) {
                tools.saveWorkspace(vaultPath = vaultPath, workspace = after)
            }
        }
        readPreviews()
    }

    private fun changeTree(
        intent: WorkspaceIntent.OnTreeChange
    ) {
        reduce { it.copy(tree = intent.tree) }
        val stale = coverSources.filter { (path, source) -> sourceOf(path = path) != source }.keys
        if (stale.isNotEmpty()) {
            stale.forEach { coverSources.remove(key = it) }
            reduce { it.copy(covers = it.covers - stale) }
        }
        if (intent.isLoaded) {
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
        val existing = current.tree.notes.keys + current.tree.attachments.keys + current.tree.folders
        val opened = current.workspace.panes.flatMap { it.tabs }
        val restored = opened.fold(initial = workspace.keepingOnly(existing = existing)) { tabs, path ->
            tabs.open(path)
        }
        reduce { it.copy(workspace = restored, isRestored = true) }
        launchGuarded(onError = ::showUnexpectedError) {
            tools.saveWorkspace(vaultPath = vaultPath, workspace = restored)
        }
        readPreviews()
    }

    /** Reads the file shown by each pane's tab, when it is a file and was not read yet or changed on disk. */
    private fun readPreviews() {
        val current = state.value
        current.workspace.panes.mapNotNull { it.activePath }.forEach { path ->
            val file = current.tree.attachments[path] ?: return@forEach
            if (current.previews[path]?.file == file) return@forEach
            launchGuarded(onError = ::showUnexpectedError) {
                val preview = filePreviews.of(file = file)
                reduce { it.copy(previews = it.previews + (path to preview)) }
            }
        }
    }

    /** Reads the picture of a folder grid's card: the image itself, or the first image a note embeds. */
    private fun readCover(
        path: RelativePath
    ) {
        if (path in coverSources) return
        coverSources[path] = sourceOf(path = path)
        launchGuarded(onError = ::showUnexpectedError) {
            val tree = state.value.tree
            val picture = if (path.isMarkdown) {
                tools.getNoteCover(vaultPath = vaultPath, note = path, files = tree.attachments.keys)
            } else {
                path.takeIf { AttachmentKind.of(name = it.name) == AttachmentKind.IMAGE }
            }
            val image = picture?.let { imageOf(path = it) }
            reduce { it.copy(covers = it.covers + (path to image)) }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun imageOf(
        path: RelativePath
    ): ImageBitmap? {
        val bytes = (tools.attachmentStore.read(vaultPath = vaultPath, path = path) as? Outcome.Success)?.value
            ?: return null
        return withContext(context = tools.dispatchers.default) {
            try {
                bytes.decodeToImageBitmap()
            } catch (_: IllegalArgumentException) {
                // Not a picture Skia can read (a broken or renamed file): the card shows its icon instead.
                null
            }
        }
    }

    private fun sourceOf(
        path: RelativePath
    ): NoteFile? = state.value.tree.let { it.notes[path]?.file ?: it.attachments[path] }

    private fun updateSidePanel(
        change: (SidePanelPreferences) -> SidePanelPreferences
    ) {
        val panel = change(state.value.sidePanel)
        reduce { it.copy(sidePanel = panel) }
        launchGuarded(onError = ::showUnexpectedError) { tools.saveSidePanel(preferences = panel) }
    }

    private fun showUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
    }

    companion object {
        private const val LOG_TAG = "WorkspaceViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the editor's tabs"
    }
}
