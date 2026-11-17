package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.change.SingularTask
import app.notekea.chat.domain.tool.work.AskUserTool

/**
 * What a call is held back for before it is made, told to the model once each: the loop's checks, in order, each
 * made only when those before it let the call through. Null when the call goes on.
 */
internal object TurnChecks {
    fun heldOf(
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? {
        // A question about notes already settled is not asked, read or not.
        val settled = MemoryChoice.askedOf(
            call = call,
            state = state
        )

        if (settled != null) return settled

        val only = oneOptionOf(
            call = call,
            state = state
        )

        if (only != null) return only

        val unread = UnreadChoice.of(
            call = call,
            state = state
        )

        if (call.name == ProposeTool.NAME) {
            ProposalReview.dropped(
                call = call,
                state = state
            )
        }

        return when {
            unread != null -> unread

            tool == null -> "(No tool \"${call.name}\". Call one of the tools listed.)"

            isEmptyProposal(call = call, state = state) -> NOTHING_ADDED

            isNeedlessQuestion(call = call, state = state) -> NOT_ASKED

            else -> WebChecks.heldOf(
                call = call,
                state = state,
                tool = tool
            ) ?: SameNoteQuestion.heldOf(
                call = call,
                state = state
            ) ?: MemoryChoice.heldOf(
                call = call,
                state = state,
                tool = tool
            ) ?: UnsettledEnd.heldOf(
                call = call,
                state = state,
                tool = tool
            ) ?: ProposalReview.heldOf(
                call = call,
                state = state
            )
        }
    }

    // Ended on propose with no change added, in a work that changes: told once, and the call asked again.
    private fun isEmptyProposal(
        call: ToolCall,
        state: TurnState
    ): Boolean {
        val changes = state
            .tools
            .tools
            .any {
                it.effect == ToolEffect.PROPOSES
            }

        val isEmpty =
            call.name == ProposeTool.NAME &&
                changes &&
                state.turn.work.changes.isEmpty()

        return isEmpty && state.told.add(NOTHING_ADDED)
    }

    // A question asked when the task points at nothing it leaves open (my two budgets, the Venezuelan recipes):
    // held back once, the model told to do it; asked again, it is asked.
    private fun isNeedlessQuestion(
        call: ToolCall,
        state: TurnState
    ): Boolean {
        val isOpen = SingularTask.pointsAtOne(task = state.task) || state.ambiguous.isNotEmpty()
        val isNeedless = call.name == AskUserTool.NAME && !isOpen

        return isNeedless && state.told.add(NOT_ASKED)
    }

    // A question with one note as its only answer: the 4B found the one recipe of two servings, then asked "which
    // recipe? Options: Pancakes" (the loop's fifth run). Held back twice, as it asks again right away.
    private fun oneOptionOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        if (call.name != AskUserTool.NAME) return null

        val only = AskUserTool
            .optionsOf(call = call)
            .singleOrNull()
            ?.let { state.turn.pathOf(name = it) } ?: return null

        val isHeld = state.told.add(ONE_OPTION) || state.told.add(ONE_OPTION_AGAIN)

        val held = "(Not asked: one note is nothing to choose between. Do the task with $only now.)"

        return held.takeIf { isHeld }
    }

    private const val ONE_OPTION = "one option"
    private const val ONE_OPTION_AGAIN = "one option again"

    private const val NOT_ASKED =
        "(Not asked: the task names what it means, all of them, so there is nothing to choose. Do it with " +
            "what the tools found. Ask only if something it needs cannot be found.)"

    private const val NOTHING_ADDED =
        "(Nothing is added to the proposal yet, so there is nothing to confirm. Call the tool of each " +
            "change first (write_note for a note to write), then propose.)"
}
