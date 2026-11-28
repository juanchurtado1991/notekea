package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.app.DaysTool
import app.notekea.chat.domain.tool.app.FigureReason
import app.notekea.chat.domain.tool.app.TallyTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.domain.figures.FigureValues
import app.notekea.core.notes.domain.RelativePath

/**
 * A conclusion drawn from text not read whole: a note the call names, unread (the E2B summed "Suppliers" orders it
 * never read, its tally refused and made again), or search results alone. Told to read, the E2B answered unread
 * (blind pool, seed 10, 2026-11-27); so the loop reads the note itself, each once, and the model works it out again.
 */
internal object UnreadConclusion {
    /** The read the loop makes before [call]: the first unread note it names, else the search's first, else null. */
    fun readOf(
        call: ToolCall,
        state: TurnState
    ): ReadFirst? {
        val reader = state.tools[ReadTool.NAME]
        if (reader == null || call.name !in CONCLUDING) return null

        val named = namedOf(
            call = call,
            state = state
        )

        val note = named ?: searchedOf(state = state) ?: return null

        val why = if (named != null) {
            "names ${note.bare()}, which you had not read"
        } else {
            "came from search results, which show only part of a note"
        }

        return ReadFirst(
            call = ToolCall(
                name = ReadTool.NAME,
                arguments = mapOf(ReadTool.NOTE to note.bare())
            ),
            tool = reader,
            told = "(Not done: ${call.name} $why. ${note.bare()} is read above, whole: work it out again from it.)"
        )
    }

    // The unread note the call names first, its reason left aside, told once each, unless quoted from a note read.
    private fun namedOf(
        call: ToolCall,
        state: TurnState
    ): RelativePath? {
        val written = call
            .arguments
            .filterKeys {
                it != FigureReason.WHY
            }
            .values
            .joinToString(separator = "\n")

        val read = state.turn.reads.read
        val readLines = readLinesOf(state = state)

        return state
            .turn
            .looked
            .map { it.file.path }
            .filter {
                it.isMarkdown &&
                    it !in read &&
                    it.nameWithoutExtension.length >= MIN_NAME
            }
            .filterNot {
                isQuoted(
                    name = it.nameWithoutExtension,
                    written = written,
                    readLines = readLines
                )
            }
            .mapNotNull { path ->
                TurnSources
                    .namedAt(written, path.nameWithoutExtension)
                    ?.let {
                        path to it
                    }
            }
            .sortedBy { (_, at) -> at }
            .map { (path, _) -> path }
            .firstOrNull { state.told.add(element = "$NAMED $it") }
    }

    // A name quoted from a note read: a line of it writes the name and a figure the call writes with it. The E2B's
    // blind pool, seed 10, 2026-11-27: "Deposits" rows, each linking a client, had every client read and the tally
    // refused, one by one; a link of Home's, with no figure, left "Suppliers" unread and summed from a search.
    private fun isQuoted(
        name: String,
        written: String,
        readLines: List<String>
    ): Boolean {
        val figures = written
            .lines()
            .filter {
                TurnSources.namedAt(it, name) != null
            }
            .flatMap { FigureValues.allIn(text = it) }

        return readLines.any { line ->
            val isNamed = TurnSources.namedAt(line, name) != null

            isNamed &&
                FigureValues.allIn(text = line).any { value ->
                    figures.any {
                        FigureValues.isSame(
                            one = it,
                            other = value
                        )
                    }
                }
        }
    }

    // The lines the notes read so far gave, as the model was shown them.
    private fun readLinesOf(state: TurnState): List<String> = state
        .foundAt
        .filterKeys { it.startsWith(prefix = "${ReadTool.NAME}:") }
        .values
        .mapNotNull { state.found.getOrNull(index = it) }
        .flatMap { it.lines() }

    // With nothing read, the first note the search showed, once.
    private fun searchedOf(state: TurnState): RelativePath? {
        val shown = state
            .turn
            .passages
            .firstOrNull()
            ?.note

        val isFirst =
            shown != null &&
                state.turn.reads.read.isEmpty() &&
                state.told.add(element = SEARCHED)

        return shown?.takeIf { isFirst }
    }

    // The calls that conclude from the text found: the answer, and the figures worked out from it.
    private val CONCLUDING =
        setOf(AnswerTool.NAME, TallyTool.NAME, DaysTool.NAME, CalculateTool.NAME)

    private const val MIN_NAME = 4
    private const val NAMED = "unread named"
    private const val SEARCHED = "searched unread"
}
