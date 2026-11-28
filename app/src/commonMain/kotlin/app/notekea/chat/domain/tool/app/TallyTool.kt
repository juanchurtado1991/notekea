package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.loop.FrameWords
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.DistinctCalls
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.domain.figures.FigureValues
import app.notekea.core.notes.domain.NoteTextCache

/**
 * Figures from lines of the notes read, worked out by the app (`docs/agent-figures.md`): the model names the lines
 * that count and those left out with why; each figure must be written on its line, or the item is refused.
 */
class TallyTool(private val noteTextCache: NoteTextCache) :
    ChatTool,
    DistinctCalls {
    override val name = NAME

    override val description =
        "figures from lines of the notes you read, worked out by the app: ${FigureReason.TEXT}, how (sum, average, count, product, " +
            "min, max or difference), items (each \"figure | the line it is on\", one for every line that counts), " +
            "left_out (each \"line | why it does not count\": refunded, paid, another year). Never add up yourself."

    override val arguments = listOf(
        ToolArgument(
            name = FigureReason.WHY,
            isReason = true
        ),
        ToolArgument(name = HOW),
        ToolArgument(
            name = ITEMS,
            isList = true
        ),
        ToolArgument(
            name = LEFT_OUT,
            isRequired = false,
            isList = true
        )
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = "${call.text(name = HOW)} of ${call.list(name = ITEMS).size}"

    // Two sums of three lines are two calls: a tally refused, then made again with its figures put right, was told
    // "its result is above" and never worked out.
    override fun sameAs(
        call: ToolCall,
        context: ToolContext
    ): String =
        "${call.text(name = HOW)} of ${call.list(name = ITEMS).joinToString(separator = SEPARATOR)}"

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val how = Tally.entries.firstOrNull {
            it.written == call.text(name = HOW).lowercase()
        }
            ?: return ToolResult(forModel = "tally: how is one of ${Tally.entries.joinToString { it.written }}.")

        val items = call.list(name = ITEMS).filter { it.isNotBlank() }
        if (items.isEmpty()) return ToolResult(forModel = "tally: no items. Give each \"figure | line\".")

        val lines = linesOf(context = context)

        val used = mutableSetOf<Int>()

        val checked = items.map {
            checkedOf(
                item = it,
                lines = lines,
                used = used
            )
        }

        val refused = checked.filter {
            it.value == null
        }

        if (refused.isNotEmpty()) {
            return ToolResult(
                forModel = "tally: not worked out. ${refused.joinToString(separator = " ") { it.problem }} Read " +
                    "the note each figure is in, then call tally again with the figure as that line writes it."
            )
        }

        val values = checked.mapNotNull { it.value }
        val result = how.of(values = values)

        val counted = checked
            .joinToString(separator = "\n") {
                "- ${Arithmetic.written(value = it.value ?: 0.0)}: ${it.line}"
            }

        val left = call
            .list(name = LEFT_OUT)
            .filter { it.isNotBlank() }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n",
                prefix = "\nLeft out:\n"
            ) { "- $it" }
            .orEmpty()

        return ToolResult(
            forModel = "tally ${how.written} of ${values.size}: ${Arithmetic.written(value = result)}\n" +
                "Counted:\n$counted$left"
        )
    }

    // The lines of every note the turn read; with none read yet, of every note, as a figure may come from a search.
    private suspend fun linesOf(context: ToolContext): List<NoteLine> {
        val read = context
            .reads
            .read
            .toSet()

        val notes = context
            .notes
            .filter {
                it.file.path.isMarkdown && (read.isEmpty() || it.file.path in read)
            }

        return noteTextCache
            .textsOf(
                vaultPath = context.vaultPath,
                notes = notes
            )
            .flatMap { (path, text) ->
                text
                    .body
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map {
                        NoteLine(
                            text = it,
                            note = path.bare()
                        )
                    }
            }
    }

    // A line of a note read, with the note's name: an item may name the note, not the line's own words. The E2B's
    // blind pool, seed 10, 2026-11-27: "1.240 € | Pedido maderas octubre" was refused, its line a table row.
    private class NoteLine(
        val text: String,
        val note: String
    )

    private class Checked(
        val line: String,
        val problem: String,
        val value: Double?
    )

    // The item's figure, on the line of the notes that writes it and shares the most words with the item's line; a
    // line already counted only when no other is as close. The first line sharing "2026" was the Counted lines once,
    // the paid sessions shown for the unpaid ones, and the 4B summed again (the loop's dev run, 2026-11-19).
    private fun checkedOf(
        item: String,
        lines: List<NoteLine>,
        used: MutableSet<Int>
    ): Checked {
        val figure = item.substringBefore(delimiter = SEPARATOR)

        val said = item.substringAfter(
            delimiter = SEPARATOR,
            missingDelimiterValue = ""
        )

        val value = FigureValues.firstIn(text = figure) ?: FigureValues.firstIn(text = item)
            ?: return Checked(
                value = null,
                line = item,
                problem = "\"$item\" holds no figure."
            )

        val words = FrameWords.of(text = said)

        val index = lines
            .indices
            .filter {
                isWritten(
                    line = lines[it].text,
                    value = value
                )
            }
            .map {
                it to sharedOf(
                    words = words,
                    line = "${lines[it].text} ${lines[it].note}"
                )
            }
            .filter { (_, shared) ->
                words.isEmpty() || shared > 0
            }
            .maxWithOrNull(
                comparator = compareBy<Pair<Int, Int>> { (_, shared) -> shared }
                    .thenBy { (at, _) ->
                        if (at in used) 0 else 1
                    }
                    .thenByDescending { (at, _) -> at }
            )
            ?.first
            ?: return Checked(
                value = null,
                line = item,
                problem = "${Arithmetic.written(value = value)} is not written on such a line of the notes read."
            )

        used += index

        return Checked(
            value = value,
            line = lines[index].text.take(n = LINE),
            problem = ""
        )
    }

    private fun isWritten(
        line: String,
        value: Double
    ): Boolean = FigureValues.allIn(text = line).any {
        FigureValues.isSame(
            one = it,
            other = value
        )
    }

    private fun sharedOf(
        words: Set<String>,
        line: String
    ): Int {
        val lineWords = FrameWords.of(text = line)

        return words.count { word ->
            lineWords.any { FrameWords.isSame(word, it) }
        }
    }

    private enum class Tally(val written: String) {
        SUM("sum"),
        AVERAGE("average"),
        COUNT("count"),
        PRODUCT("product"),
        MIN("min"),
        MAX("max"),
        DIFFERENCE("difference");

        fun of(values: List<Double>): Double = when (this) {
            SUM -> values.sum()

            AVERAGE -> values.average()

            COUNT -> values.size.toDouble()

            PRODUCT ->
                values
                    .fold(initial = 1.0) { product, value ->
                        product * value
                    }

            MIN -> values.min()

            MAX -> values.max()

            DIFFERENCE -> values.first() - values.drop(n = 1).sum()
        }
    }

    companion object {
        const val NAME = "tally"
        const val HOW = "how"
        const val ITEMS = "items"
        const val LEFT_OUT = "left_out"
        private const val SEPARATOR = "|"
        private const val LINE = 140
    }
}
