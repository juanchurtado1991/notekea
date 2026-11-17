package app.notekea.chat.domain.loop

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.app.ShowTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.notes.domain.RelativePath

/**
 * An end that picks one of the notes a task can mean and does not say which of ([AmbiguousTask]): held back once,
 * and the next call may only ask the person (`docs/agent-loop.md`, "Why the loop fails", item 3).
 */
internal object UnsettledEnd {
    /** What the model is told when [call] ends without asking which of the notes the task left open, else null. */
    fun heldOf(
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? {
        val isEnd = tool is ShowTool || tool?.effect == ToolEffect.ENDS
        if (!isEnd || call.name == AskUserTool.NAME) return null

        val open = state.ambiguous

        // A task that means one note is not settled by an end naming them all: "Laura's phone" answered with both
        // Lauras' (the loop's eighth run, the bug hunt of 2026-11-17, L10). Only a task about several may cover them.
        val isCovered = !AmbiguousTask.isSingular(task = state.task) && AmbiguousTask.coversAll(
            paths = open,
            text = endOf(call = call, state = state)
        )

        val isSettled = open.isEmpty() || isCovered

        if (isSettled) return null

        if (!state.told.add(MUST_ASK)) return null

        val named = open.joinToString {
            it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
        }

        return "(Not done: the task can mean ${open.size} notes and does not say which: $named. Ask the person " +
            "with ask_user, these as the options.)"
    }

    /** Whether the next call may only be ask_user: the turn was held back for a question it must ask. */
    fun mustAsk(state: TurnState): Boolean = MUST_ASK in state.told

    // What the end says and acts on: its text, the notes it shows, and the changes it proposes.
    private fun endOf(
        call: ToolCall,
        state: TurnState
    ): String {
        val changed = state
            .turn
            .work
            .changes
            .flatMap {
                it.notes + listOfNotNull(it[ActionField.NOTE])
            }
            .mapNotNull { state.turn.pathOf(name = it) }

        return (call.arguments.values + changed).joinToString(separator = "\n")
    }

    const val MUST_ASK = "must ask"
}
