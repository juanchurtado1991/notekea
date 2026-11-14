package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.language.ReplyLanguage
import app.notekea.chat.domain.plan.PageProgress
import app.notekea.chat.domain.plan.SectionAsked
import app.notekea.chat.domain.sources.SourcePage

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
    // A clip of many pages under way: how far it is, shown under the step's line.
    val progress: PageProgress? = null,
    val moves: List<AgentMoveLine> = emptyList(),
    // The steps it already left, oldest first: the checked lines over the one it is on.
    val passed: List<PassedStep> = emptyList(),
    // The notes did not hold the answer: the sites of the vault's web sources it may be looked for in ([sites]),
    // then their pages that may hold it ([sitePages], null until looked for), the ones ticked [pickedPages].
    val sites: List<String> = emptyList(),
    val sitePages: List<SourcePage>? = null,
    val pickedPages: Set<String> = emptySet(),
    val isLookingInSites: Boolean = false,
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
