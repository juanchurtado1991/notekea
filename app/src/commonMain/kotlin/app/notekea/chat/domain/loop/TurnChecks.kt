package app.notekea.chat.domain.loop

import app.notekea.chat.domain.loop.TaskFrame.Companion.bare
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.change.ProposeTool
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
        val only = oneOptionOf(
            call = call,
            state = state
        )

        if (only != null) return only

        val settled = settledOf(
            call = call,
            state = state
        )

        if (settled != null) return settled

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
            ) ?: FrameChoice.heldOf(
                call = call,
                state = state,
                tool = tool
            ) ?: SeveralUnread.heldOf(
                call = call,
                state = state
            ) ?: UnsettledEnd.heldOf(
                call = call,
                state = state,
                tool = tool
            ) ?: TopicUnread.heldOf(
                call = call,
                state = state
            ) ?: AnswerFigures.heldOf(
                call = call,
                state = state
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

    // A question asked when the frame says the task leaves nothing open (my two budgets, the Venezuelan recipes,
    // "Ana Torres"): held back once, the model told to do it; asked again, it is asked, as it may lack something no
    // tool finds. With no frame, nothing says it is needless.
    private fun isNeedlessQuestion(
        call: ToolCall,
        state: TurnState
    ): Boolean {
        val isOpen = state.frame?.isOpen ?: true

        val isNeedless =
            call.name == AskUserTool.NAME &&
                !isOpen &&
                !isAskedFor(state = state)

        return isNeedless && state.told.add(NOT_ASKED)
    }

    // A question whose options are notes or entries the frame settled, the question it already answered
    // (`docs/loop-goal-plan.md`, step 2.4): held back every time, as once left the model to ask again, and after the
    // second time the next call may not ask. A question about something else goes through [isNeedlessQuestion].
    private fun settledOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        val frame = state.frame ?: return null

        val isSettled =
            call.name == AskUserTool.NAME &&
                !frame.isOpen &&
                frame.notes.isNotEmpty() &&
                !isAskedFor(state = state)

        if (!isSettled) return null

        val entries = frame.targets.flatMap { it.entries }

        val isWhich = AskUserTool
            .optionsOf(call = call)
            .any { option ->
                val isNote = state.turn.noteNamed(name = option) in frame.candidates

                isNote || entries.any {
                    it.contains(
                        other = option.trim(),
                        ignoreCase = true
                    )
                }
            }

        if (!isWhich) return null

        val isAgain = state.told.any { it.startsWith(prefix = SETTLED) }

        state.told.add("$SETTLED ${state.told.size}")
        if (isAgain) state.withheld += AskUserTool.NAME

        val meant = frame.entry ?: frame.notes.joinToString { it.bare() }

        return "(Not asked: the task already says which, $meant. Do the task with it now; ask only for something " +
            "it needs that no note gives.)"
    }

    // A question with one note as its only answer: the 4B found the one recipe of two servings, then asked "which
    // recipe? Options: Pancakes" (the loop's fifth run). Held back twice, as it asks again right away.
    private fun oneOptionOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        val isAsking = call.name == AskUserTool.NAME && !isAskedFor(state = state)
        if (!isAsking) return null

        val only = AskUserTool
            .optionsOf(call = call)
            .singleOrNull()
            ?.let { state.turn.pathOf(name = it) } ?: return null

        val isHeld = state.told.add(ONE_OPTION) || state.told.add(ONE_OPTION_AGAIN)

        val held = "(Not asked: one note is nothing to choose between. Do the task with $only now.)"

        return held.takeIf { isHeld }
    }

    // A question the loop itself told the model to ask: where the person lives, which of the notes the task left
    // open, or a change tool's "ask_user when the task does not say". Holding it back left the model nothing to do
    // (the bug hunt of 2026-11-17, L12).
    private fun isAskedFor(state: TurnState): Boolean {
        val isTold = state.told.any {
            it in ASKED_FOR
        }

        val isLastAsking = state
            .found
            .lastOrNull()
            ?.contains(other = AskUserTool.NAME) == true

        return isTold || isLastAsking
    }

    private val ASKED_FOR = setOf(WebChecks.COUNTRY_UNKNOWN, UnsettledEnd.MUST_ASK)

    private const val ONE_OPTION = "one option"
    private const val SETTLED = "settled asked"
    private const val ONE_OPTION_AGAIN = "one option again"

    private const val NOT_ASKED =
        "(Not asked: the task names what it means, all of them, so there is nothing to choose. Do it with " +
            "what the tools found. Ask only if something it needs cannot be found.)"

    private const val NOTHING_ADDED =
        "(Nothing is added to the proposal yet, so there is nothing to confirm. Call the tool of each " +
            "change first (write_note for a note to write), then propose.)"
}
