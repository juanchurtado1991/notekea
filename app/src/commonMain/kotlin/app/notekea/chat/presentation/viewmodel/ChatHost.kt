package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatState
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.ViewModelHost

/**
 * What the chat's parts (answering, actions, agent mode) share with [ChatViewModel], which routes the intents to them:
 * besides the state, the vault's [notes] and the note open in the window ([activeNote]), one turn's change
 * ([update]) and an answer's next step shown in its turn ([follow]).
 */
internal interface ChatHost : ViewModelHost<ChatState, ChatEffect> {
    val notes: Collection<NoteEntry>
    val activeNote: RelativePath?

    /** Where the person is, as the model may be told ([ChatState.shared]). */
    val working: WorkContext?

    fun update(
        id: Long,
        change: (ChatEntry) -> ChatEntry
    )

    fun follow(
        id: Long,
        step: AnswerStep
    )
}
