package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.LinkUpdateResult
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Renaming in the sidebar (`ROADMAP.md` 1.4): the name typed in place, confirmed once even when Enter and the field
 * losing focus both confirm it; after a rename the notes that link to the entry are updated in the background, as
 * Obsidian does by default, one rename's links at a time while the next rename goes on, the bookmarks follow it, and a
 * note made from a template is filled once named.
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
    private val updatingLinks = Mutex()
    private var job: Job? = null
    private var renames = 0

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
            val outcome = renameEntry(vaultPath = vaultPath, path = draft.path, newName = draft.text)
            val isMoved = outcome is Outcome.Success && outcome.value != draft.path
            if (isMoved) host.emit(effect = VaultTreeEffect.EntryRenamed(from = draft.path, to = outcome.value))
            host.updateTree { reduceRenameOutcome(state = it, from = draft.path, outcome = outcome) }
            if (isMoved) {
                renames++
                bookmarks.change { it.renamed(from = draft.path, to = outcome.value) }
            }
            host.launch {
                if (isMoved) updateLinks(from = draft.path, to = outcome.value)
                if (outcome is Outcome.Success) templates.apply(note = draft.path, named = outcome.value)
            }
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
        renames++
        bookmarks.change { it.renamed(from = intent.from, to = intent.to) }
        intent.links?.let { result -> host.reduce { reduceLinksUpdated(state = it, result = result) } }
    }

    /** Rewrites the links to [from] in the notes that had them, now that it is [to]; nothing when none link to it. */
    private suspend fun updateLinks(
        from: RelativePath,
        to: RelativePath
    ) {
        val result = updatingLinks.withLock { linksUpdated(from = from, to = to) } ?: return
        logger.i { LINKS_UPDATED_MESSAGE + result.updatedCount + NOTES_SKIPPED + result.skipped.size }
        host.emit(effect = VaultTreeEffect.LinksRewritten(notes = result.updated))
        host.reduce { reduceLinksUpdated(state = it, result = result) }
    }

    /** A rename meanwhile may have moved a note from under the update, so the vault is gone through again. */
    private suspend fun linksUpdated(
        from: RelativePath,
        to: RelativePath
    ): LinkUpdateResult? {
        val updated = mutableListOf<RelativePath>()
        var skipped = emptyList<RelativePath>()
        do {
            val seen = renames
            val notesBefore = host.state.value.tree.notes.keys.mapTo(destination = mutableSetOf()) {
                it.rebased(from = to, to = from)
            }
            val pass = updateLinksAfterRename(vaultPath = vaultPath, notesBefore = notesBefore, from = from, to = to)
            updated += pass?.updated.orEmpty()
            skipped = pass?.skipped.orEmpty()
        } while (seen != renames)
        val result = LinkUpdateResult(updated = updated, skipped = skipped)
        return result.takeIf { updated.isNotEmpty() || skipped.isNotEmpty() }
    }

    companion object {
        private const val LINKS_UPDATED_MESSAGE = "Links updated after a rename: "
        private const val LOG_TAG = "TreeRenaming"
        private const val NOTES_SKIPPED = " notes, skipped: "
    }
}
