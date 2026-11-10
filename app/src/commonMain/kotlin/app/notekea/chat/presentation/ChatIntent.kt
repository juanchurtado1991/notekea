package app.notekea.chat.presentation

import app.notekea.chat.domain.AnswerSource
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.FoundRow
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

    /** Deep answer on or off (3.6): three drafts and a vote, slower and more often right. */
    data object OnDeepToggle : ChatIntent

    data object OnModelSettingsOpen : ChatIntent

    /** The panel shows the chat: the model in use is looked at again, a download may have ended meanwhile. */
    data object OnShown : ChatIntent

    data class OnNotesChange(val notes: Collection<NoteEntry>) : ChatIntent

    data class OnIndexChange(val isReady: Boolean) : ChatIntent

    data class OnActiveNoteChange(val path: RelativePath?) : ChatIntent

    /** The action card of [id]: Do it, Cancel, or Undo once done (3.9). */
    data class OnActionConfirm(val id: Long) : ChatIntent

    data class OnActionCancel(val id: Long) : ChatIntent

    data class OnActionUndo(val id: Long) : ChatIntent

    /** A row of a table the chat found: its note opens, at the task's line for a task. */
    data class OnFoundClick(val row: FoundRow) : ChatIntent
}
