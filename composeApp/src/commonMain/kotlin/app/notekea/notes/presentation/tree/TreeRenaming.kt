package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job

/**
 * Renaming in the sidebar (`ROADMAP.md` 1.4): the name typed in place, confirmed once even when Enter and the field
 * losing focus both confirm it; after a rename the notes that link to the entry are updated in the background, as
 * Obsidian does by default, the bookmarks follow it, and a note made from a template is filled once named.
 */
internal class TreeRenaming(
    private val host: TreeHost,
    private val vaultPath: String,
    private val renameEntry: RenameEntryUseCase,
    private val updateLinksAfterRename: UpdateLinksAfterRenameUseCase,
    private val bookmarks: TreeBookmarks,
    private val templates: TreeTemplates
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var job: Job? = null

    fun start(
        path: RelativePath?
    ) {
        val target = path ?: host.state.value.selectedPath ?: return
        host.reduce { reduceRenameStart(state = it, path = target) }
    }

    fun changeText(
        text: String
    ) {
        host.reduce { it.copy(renaming = it.renaming?.copy(text = text, error = null)) }
    }

    fun confirm() {
        val draft = host.state.value.renaming ?: return
        if (job?.isActive == true) return
        job = host.launch {
            val notesBefore = host.state.value.tree.notes.keys
            val outcome = renameEntry(vaultPath = vaultPath, path = draft.path, newName = draft.text)
            val isMoved = outcome is Outcome.Success && outcome.value != draft.path
            if (isMoved) host.emit(effect = VaultTreeEffect.EntryRenamed(from = draft.path, to = outcome.value))
            host.updateTree { reduceRenameOutcome(state = it, from = draft.path, outcome = outcome) }
            if (isMoved) {
                bookmarks.change { it.renamed(from = draft.path, to = outcome.value) }
                updateLinks(notesBefore = notesBefore, from = draft.path, to = outcome.value)
            }
            if (outcome is Outcome.Success) templates.apply(note = draft.path, named = outcome.value)
        }
    }

    fun cancel() {
        val draft = host.state.value.renaming
        host.reduce { it.copy(renaming = null) }
        if (draft != null) host.launch { templates.apply(note = draft.path, named = draft.path) }
    }

    /** The editor's title renamed a note: the tree selects it at its new path and reports the links it updated. */
    fun onRenamedElsewhere(
        intent: VaultTreeIntent.OnEntryRenamed
    ) {
        host.updateTree {
            reduceRenameOutcome(state = it, from = intent.from, outcome = Outcome.Success(value = intent.to))
        }
        bookmarks.change { it.renamed(from = intent.from, to = intent.to) }
        intent.links?.let { result -> host.reduce { reduceLinksUpdated(state = it, result = result) } }
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
        host.reduce { reduceLinksUpdated(state = it, result = result) }
    }

    companion object {
        private const val LINKS_UPDATED_MESSAGE = "Links updated after a rename: "
        private const val LOG_TAG = "TreeRenaming"
        private const val NOTES_SKIPPED = " notes, skipped: "
    }
}
