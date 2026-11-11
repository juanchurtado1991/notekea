package app.notekea.chat.presentation

import app.notekea.chat.domain.AnswerSource
import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ReplyLanguage
import app.notekea.chat.domain.TurnOutcome

/**
 * A question of the chat and its answer, as the panel draws it: [id] keys it in the list. While it is answered,
 * [step] says where it stands and [sources] are the passages read, which its `[n]` cite; once [outcome] is set they
 * are the passages it cites, numbered again. A request that is not a question holds its [action] instead.
 */
data class ChatEntry(
    val id: Long,
    val question: String,
    val scope: ChatScope,
    val step: ChatStep,
    val answer: String,
    val sources: List<AnswerSource>,
    val readNotes: Int,
    val scopeName: String?,
    val outcome: TurnOutcome?,
    val error: ChatError?,
    val action: ChatAction? = null
) {
    /** The language the chat replies to it in: the question's. */
    val language: ReplyLanguage get() = ReplyLanguage.of(text = question)

    val isAnswering: Boolean get() = outcome == null
}
