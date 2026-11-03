package app.notekea.notes.presentation.tree

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameEntryUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job

/**
 * Renaming in the sidebar (`ROADMAP.md` 1.4): the name typed in place, confirmed once even when Enter and the field
 * losing focus both confirm it; after a rename the bookmarks follow the entry, a note made from a template is filled
 * once named, and when notes link to it, the links are planned and offered (`ROADMAP.md` 2.5): written only once
 * the person agrees, never silently.
 */
internal class TreeRenaming(
    private val host: TreeHost,
    private val vaultPath: String,
    private val renameEntry: RenameEntryUseCase,
    private val linkUpdates: LinkUpdateTools,
    private val bookmarks: TreeBookmarks,
    private val templates: TreeTemplates
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var job: Job? = null

    /** A rename's intents: the name typed in place, a rename from the editor's title, and the answer about links. */
    fun onIntent(
        intent: RenameIntent
    ) {
        when (intent) {
            VaultTreeIntent.OnRenameCancel -> cancel()
            VaultTreeIntent.OnRenameConfirm -> confirm()
            is VaultTreeIntent.OnRenameRequest -> start(path = intent.path)
            is VaultTreeIntent.OnRenameTextChange -> changeText(text = intent.text)
            is VaultTreeIntent.OnEntryRenamed -> onRenamedElsewhere(intent = intent)
            VaultTreeIntent.OnLinkUpdateConfirm -> updateLinks()
            VaultTreeIntent.OnLinkUpdateDecline -> host.reduce { it.copy(linkOffer = null) }
        }
    }

    private fun start(
        path: RelativePath?
    ) {
        val target = path ?: host.state.value.selectedPath ?: return
        host.reduce { reduceRenameStart(state = it, path = target) }
    }

    private fun changeText(
        text: String
    ) {
        host.reduce { it.copy(renaming = it.renaming?.copy(text = text, error = null)) }
    }

    private fun confirm() {
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
                offerLinks(notesBefore = notesBefore, from = draft.path, to = outcome.value)
            }
            if (outcome is Outcome.Success) templates.apply(note = draft.path, named = outcome.value)
        }
    }

    private fun cancel() {
        val draft = host.state.value.renaming
        host.reduce { it.copy(renaming = null) }
        if (draft != null) host.launch { templates.apply(note = draft.path, named = draft.path) }
    }

    /** The editor's title renamed a note: the tree selects it at its new path and offers the links it planned. */
    private fun onRenamedElsewhere(
        intent: VaultTreeIntent.OnEntryRenamed
    ) {
        host.updateTree {
            reduceRenameOutcome(state = it, from = intent.from, outcome = Outcome.Success(value = intent.to))
        }
        bookmarks.change { it.renamed(from = intent.from, to = intent.to) }
        offer(offer = LinkUpdateOffer(from = intent.from, to = intent.to, rewrites = intent.rewrites))
    }

    /** The person agreed: the offered links are written, a note changed meanwhile left alone, and the notice says so. */
    private fun updateLinks() {
        val offer = host.state.value.linkOffer ?: return
        host.reduce { it.copy(linkOffer = null) }
        host.launch {
            val result = linkUpdates.applyLinkUpdates(vaultPath = vaultPath, rewrites = offer.rewrites)
            logger.i { LINKS_UPDATED_MESSAGE + result.updatedCount + NOTES_SKIPPED + result.skipped.size }
            host.reduce { reduceLinksUpdated(state = it, result = result) }
        }
    }

    /** Plans the links to [from] that must name [to] now, and offers them; nothing when no note links to it. */
    private suspend fun offerLinks(
        notesBefore: Set<RelativePath>,
        from: RelativePath,
        to: RelativePath
    ) {
        val rewrites = linkUpdates.planLinkUpdates(
            vaultPath = vaultPath,
            notesBefore = notesBefore,
            from = from,
            to = to
        )
        offer(offer = LinkUpdateOffer(from = from, to = to, rewrites = rewrites))
    }

    private fun offer(
        offer: LinkUpdateOffer
    ) {
        if (offer.rewrites.isNotEmpty()) host.reduce { it.copy(linkOffer = offer) }
    }

    companion object {
        private const val LINKS_UPDATED_MESSAGE = "Links updated after a rename: "
        private const val LOG_TAG = "TreeRenaming"
        private const val NOTES_SKIPPED = " notes, skipped: "
    }
}
