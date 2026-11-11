package app.notekea.chat.presentation

import app.notekea.chat.domain.AnswerSource
import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.ReplyLanguage
import app.notekea.chat.domain.SectionAsked
import app.notekea.chat.domain.TurnOutcome

/**
 * A question of the chat and its answer, keyed by [id]: [step] says where it stands, [sources] are the passages its
 * `[n]` cite, an action holds its [action], a help answer may offer a request ([offered], "…" where they finish it),
 * and [answeredWith] is the mode Auto, or the person, used for it.
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
    val action: ChatAction? = null,
    val offered: String? = null,
    // A pasted page that links pages under its path: the chat asks whether to clip only it or them too.
    val sectionAsked: SectionAsked? = null,
    val moves: List<AgentMoveLine> = emptyList(),
    val answeredWith: ChatMode? = null
) {
    /**
     * Whether the history keeps it: once answered, and for a request the chat acted on only once done, since one
     * refused, cancelled or undone changed nothing.
     */
    val isKept: Boolean
        get() {
            val status = action?.status ?: return !isAnswering

            return status == ActionStatus.DONE || status == ActionStatus.FINISHED
        }

    fun toTurn(): ChatTurn = ChatTurn(
        question = question,
        scope = scope,
        outcome = outcome ?: TurnOutcome.STOPPED,
        answer = answer,
        sources = sources,
        scopeName = scopeName
    )

    /** The language the chat replies to it in: the question's. */
    val language: ReplyLanguage by lazy { ReplyLanguage.of(text = question) }

    val isAnswering: Boolean get() = outcome == null

    companion object {
        /** A [turn] of the history, shown again as entry [id]. */
        fun of(
            turn: ChatTurn,
            id: Long
        ): ChatEntry = ChatEntry(
            id = id,
            question = turn.question,
            scope = turn.scope,
            step = ChatStep.DONE,
            answer = turn.answer,
            sources = turn.sources,
            readNotes = 0,
            scopeName = turn.scopeName,
            outcome = turn.outcome,
            error = null
        )
    }
}
