package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.presentation.viewmodel.after
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

sealed interface ChatIntent {
    data class OnQuestionChange(val text: String) : ChatIntent

    data object OnSend : ChatIntent

    /** Stop (`Esc`): what was written is kept. */
    data object OnStop : ChatIntent

    data class OnScopeSelect(val scope: ChatScope) : ChatIntent

    /** A citation or a source was clicked: its note opens with the passage selected. */
    data class OnSourceClick(val source: AnswerSource) : ChatIntent

    /** Asks the question of [id] again, after it failed. */
    data class OnRetry(val id: Long) : ChatIntent

    data object OnClearHistory : ChatIntent

    /** Copy chat: the whole conversation on the clipboard, to report an answer that went wrong. */
    data object OnCopyChat : ChatIntent

    /** Compact chat: what the chat remembers of itself written short, as Claude Code's /compact. */
    data object OnCompact : ChatIntent

    data object OnModelSettingsOpen : ChatIntent

    /** The panel shows the chat: the model in use is looked at again, a download may have ended meanwhile. */
    data object OnShown : ChatIntent

    data class OnNotesChange(val notes: Collection<NoteEntry>) : ChatIntent

    data class OnIndexChange(val isReady: Boolean) : ChatIntent

    data class OnActiveNoteChange(val path: RelativePath?) : ChatIntent

    /** Where the person is changed: the tab, the section the cursor is in or the selection. */
    data class OnWorkChange(val working: WorkContext?) : ChatIntent

    /** A context chip's ×: that part is not told to the model until the person moves somewhere else. */
    data class OnWorkRemove(val part: WorkPart) : ChatIntent

    /** The action card of [id]: Do it, Cancel, or Undo once done (3.9). */
    data class OnActionConfirm(val id: Long) : ChatIntent

    /** The card's step [step] is to create its note in [folder]. */
    data class OnActionPlace(
        val id: Long,
        val step: Int,
        val folder: RelativePath
    ) : ChatIntent

    data class OnActionCancel(val id: Long) : ChatIntent

    data class OnActionUndo(val id: Long) : ChatIntent

    /**
     * The request a help answer offers ([ChatEntry.offered]): done at once, or put in the question field when it has
     * a "…" to finish.
     */
    data class OnOfferedClick(val id: Long) : ChatIntent

    /** "Look in wordpress.org": the pages of the vault's web sources' sites that may answer turn [id]. */
    data class OnSiteSearch(val id: Long) : ChatIntent

    /** A page found there, ticked or not, to be clipped. */
    data class OnSitePageToggle(
        val id: Long,
        val url: String
    ) : ChatIntent

    /** "Clip and answer": the ticked pages join their section, and the question is answered from them. */
    data class OnSiteClip(val id: Long) : ChatIntent

    /** A question the chat suggested under its last answer ([FollowUps]), asked of the whole vault. */
    data class OnFollowUp(val text: String) : ChatIntent

    /** What the chat's memory and the sources found on the web are asked, acted on apart from the rest. */
    sealed interface MemoryIntent : ChatIntent

    /** Remember was ticked under a question back and an answer chosen: [line] goes into the chat's memory. */
    data class OnRemember(val line: String) : MemoryIntent

    /** Forget, on a step that used a line of the chat's memory: [line] is taken out of it. */
    data class OnMemoryForget(val line: String) : MemoryIntent

    /** A source found on the web ticked or unticked, by its [url], under the entry [id]. */
    data class OnSourceToggle(
        val id: Long,
        val url: String
    ) : MemoryIntent

    /** Always or never trust the site [domain]: a line of the memory's Sources (docs/agent-web.md). */
    data class OnSourceTrust(
        val domain: String,
        val isTrusted: Boolean
    ) : MemoryIntent

    /** Read the ticked sources of the entry [id]: the request is asked again with their addresses. */
    data class OnSourcesRead(val id: Long) : MemoryIntent

    /** The answer to "only this page, or the pages it links too?" ([ChatEntry.sectionAsked]), asked as a request. */
    data class OnSectionChoose(
        val id: Long,
        val isWhole: Boolean,
        val isEvery: Boolean = false,
        // How many levels of links are followed: the pages the page links, then those they link…
        val depth: Int = 1
    ) : ChatIntent

    /** A row of a table the chat found: its note opens, at the task's line for a task. */
    data class OnFoundClick(val row: FoundRow) : ChatIntent
}
