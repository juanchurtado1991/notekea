package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentFinalAnswer
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.AgentTaskMove
import app.notekea.chat.domain.agent.GroundedSteps
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.agent.ProposalVault
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool

/**
 * How a turn of the loop ends, by the call that ends it: a question, the card of its changes, or an answer written
 * from what it found. A turn that tried to change the notes and added no change says so.
 */
internal class TurnEnd(
    private val finalAnswer: AgentFinalAnswer,
    private val proposal: ProposalCheck,
    private val review: ProposalReview
) {
    suspend fun stepOf(
        call: ToolCall,
        tool: ChatTool,
        state: TurnState
    ): AgentStep {
        val isEmpty = state
            .turn
            .work
            .changes
            .isEmpty()

        val isRefused = isEmpty && state.refused.isNotEmpty()

        // A refused change and then an answer that changes nothing is a turn that answered: a web turn's refused
        // append, then the right answer, ended as "No change was added" (the bug hunt of 2026-11-17, L14).
        val answer = call.text(name = AnswerTool.TEXT)

        val isClaimed =
            isRefused &&
                isChanging(state = state) &&
                CLAIMED.containsMatchIn(input = answer)

        return when (tool.name) {
            AskUserTool.NAME -> AgentStep.Asked(
                question = call.text(name = AskUserTool.QUESTION),
                options = AskUserTool.optionsOf(call = call),
                remembering = state.frame?.rememberingOf(task = state.task)
            )

            ProposeTool.NAME -> if (isEmpty) {
                unchanged(state = state)
            } else {
                reviewed(
                    call = call,
                    state = state
                )
            }

            else -> if (isClaimed) {
                unchanged(state = state)
            } else {
                AgentStep.Finished(
                    summary = finalAnswer.written(
                        task = state.task,
                        found = fittedOf(state = state),
                        summary = answer,
                        memory = state.memory.map { it.text }
                    )
                )
            }
        }
    }

    // The card of what the review left: a summary written before it would tell of notes the card no longer changes.
    private suspend fun reviewed(
        call: ToolCall,
        state: TurnState
    ): AgentStep {
        val isCut = review.review(
            state = state,
            found = fittedOf(state = state)
        )

        val summary = if (isCut) "" else call.text(name = ProposeTool.SUMMARY)

        return proposed(
            state = state,
            summary = summary
        )
    }

    // The turn's kind changes notes: its change tools are offered.
    private fun isChanging(state: TurnState): Boolean = state
        .tools
        .tools
        .any {
            it.effect == ToolEffect.PROPOSES
        }

    /** What the answer is written from: what was found, as far as the context goes. */
    suspend fun fittedOf(state: TurnState): List<String> = state
        .budget
        .fit(state.found, state.dropped)
        .entries

    /**
     * The changes the turn added, checked against the vault and written out for the card; the notes it read are
     * what a note it writes is written from.
     */
    suspend fun proposed(
        state: TurnState,
        summary: String
    ): AgentStep {
        val move = GroundedSteps.of(
            move = AgentTaskMove(
                move = ProposeTool.NAME,
                summary = summary,
                steps = state.turn.work.changes
            ),
            read = state.turn.reads.read
        )

        return proposal.stepOf(
            move = move,
            unreadable = state.turn.reads.blind,
            task = state.task,
            vault = ProposalVault(
                path = state.turn.vaultPath,
                notes = state.turn.notes,
                openNote = state.turn.openNote
            )
        )
    }

    // The loop's eighth run ended "Lowered the rating to 3 for Atomic Habits and Sapiens" with every set_property
    // refused: the summary of an empty proposal shown as done.
    private suspend fun unchanged(state: TurnState): AgentStep = AgentStep.Finished(
        summary = finalAnswer.written(
            task = state.task,
            found = fittedOf(state = state) + NOTHING_CHANGED,
            summary = "",
            memory = state.memory.map { it.text }
        )
    )

    companion object {
        // An answer that says it changed the notes: "Lowered the rating", "I have added", "añadí".
        private val CLAIMED = Regex(
            pattern = "(?<![\\p{L}])(added|appended|changed|updated|lowered|raised|set|tagged|moved|renamed|" +
                "created|wrote|written|marked|completed|removed|deleted|linked|clipped|saved|añad[íi]|" +
                "agregu[ée]|cambi[ée]|actualic[ée]|etiquet[ée]|mov[íi]|renombr[ée]|cre[ée]|escrib[íi]|" +
                "marqu[ée]|guard[ée]|elimin[ée]|borr[ée])(?![\\p{L}])",
            option = RegexOption.IGNORE_CASE
        )

        const val NOTHING_CHANGED =
            "No change was added in this turn, so the notes are as they were. The answer says so plainly, " +
                "with what stopped it, and never says a change was made."
    }
}
