package app.notekea.chat.domain.conversation

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.tool.ToolOrigin

/**
 * One thing a turn of the chat holds after its question, in the order it happened (`docs/agent-loop.md`, step 3):
 * what it looked up, what it said and what it proposed, so the chat knows later what it did.
 */
sealed interface TurnItem {
    /** A tool called: its [name] as the model names it, and what on ([target]). */
    data class Call(
        val name: String,
        val target: String
    ) : TurnItem

    /** What a tool gave back, as the model read it, and whether it is the person's notes or untrusted text. */
    data class Result(
        val text: String,
        val origin: ToolOrigin
    ) : TurnItem

    /** What the chat said, with the passages its `[n]` cite. */
    data class Text(
        val text: String,
        val sources: List<AnswerSource>
    ) : TurnItem

    /** The changes put on a card, how that ended, and the [problem] when they were not done. */
    data class Proposal(
        val changes: List<ProposedChange>,
        val outcome: ProposalOutcome,
        val problem: String?
    ) : TurnItem
}
