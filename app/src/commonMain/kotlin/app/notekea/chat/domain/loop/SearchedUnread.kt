package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.lookup.SearchTool
import app.notekea.chat.domain.tool.work.AnswerTool

/**
 * An answer given from search results alone, no note read: a result shows the start of a note, and the E2B summed
 * the two glucose readings it showed of November's five (the E2B's dev set, 2026-11-26). Held back once, the model
 * told to read the note that holds the answer first.
 */
internal object SearchedUnread {
    fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        if (call.name != AnswerTool.NAME) return null

        val hasSearched = state.made.any {
            it.substringBefore(delimiter = ':') == SearchTool.NAME
        }

        val isUnread = hasSearched && state.turn.reads.read.isEmpty()
        val isHeld = isUnread && state.told.add(element = TOLD)
        if (!isHeld) return null

        return "(Not done: you have read no note, and a search result shows only part of one. Read the note that " +
            "holds the answer, then answer from it, above all before saying the notes do not hold something.)"
    }

    private const val TOLD = "searched unread"
}
