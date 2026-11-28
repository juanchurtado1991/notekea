package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.app.DaysTool
import app.notekea.chat.domain.tool.app.TallyTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool

/**
 * A conclusion from search results alone, which show part of a note: the E2B summed 9 of the 17.5 CPD hours a log
 * held (blind pool, seed 10, 2026-11-27), and told to read, answered again unread. So the loop reads the note itself.
 */
internal object SearchedUnread {
    /** The read the loop makes once before [call]: the note it names of those the search showed, else the first. */
    fun readOf(
        call: ToolCall,
        state: TurnState
    ): ReadFirst? {
        val shown = state
            .turn
            .passages
            .map { it.note }
            .distinct()

        val reader = state.tools[ReadTool.NAME] ?: return null

        val isUnread =
            call.name in CONCLUDING &&
                shown.isNotEmpty() &&
                state.turn.reads.read.isEmpty()

        val isFirst = isUnread && state.told.add(element = TOLD)
        if (!isFirst) return null

        val written = call
            .arguments
            .values
            .joinToString(separator = "\n")
            .lowercase()

        val note = shown.firstOrNull {
            it.nameWithoutExtension.lowercase() in written
        } ?: shown.first()

        return ReadFirst(
            call = ToolCall(
                name = ReadTool.NAME,
                arguments = mapOf(ReadTool.NOTE to note.bare())
            ),
            tool = reader,
            told = "(Not done: ${call.name} came from search results, which show only part of a note. " +
                "${note.bare()} is read above, whole: work it out again from it.)"
        )
    }

    // The calls that conclude from the text found: the answer, and the figures worked out from it.
    private val CONCLUDING =
        setOf(AnswerTool.NAME, TallyTool.NAME, DaysTool.NAME, CalculateTool.NAME)

    private const val TOLD = "searched unread"
}
