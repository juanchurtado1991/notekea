package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.loop.FrameWords
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.notes.domain.NoteTextCache

/**
 * Figures from lines of the notes read, worked out by the app (`docs/agent-figures.md`): the model names the lines
 * that count and those left out with why; each figure must be written on its line, or the item is refused.
 */
class TallyTool(private val noteTextCache: NoteTextCache) : ChatTool {
    override val name = NAME

    override val description =
        "figures from lines of the notes you read, worked out by the app: how (sum, average, count, product, " +
            "min, max or difference), items (each \"figure | the line it is on\", one for every line that counts), " +
            "left_out (each \"line | why it does not count\": refunded, paid, another year). Never add up yourself."

    override val arguments = listOf(
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

        val checked = items.map {
            checkedOf(
                item = it,
                lines = lines
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
    private suspend fun linesOf(context: ToolContext): List<String> {
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
            .values
            .flatMap { it.body.lines() }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private class Checked(
        val line: String,
        val problem: String,
        val value: Double?
    )

    // The item's figure, found on a line of the notes that writes it and shares a word with the item's line.
    private fun checkedOf(
        item: String,
        lines: List<String>
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

        val line = lines.firstOrNull { line ->
            val isWritten = FigureValues.allIn(text = line).any {
                FigureValues.isSame(
                    one = it,
                    other = value
                )
            }

            val lineWords = FrameWords.of(text = line)

            val isSame = words.isEmpty() || words.any { word ->
                lineWords.any { FrameWords.isSame(word, it) }
            }

            isWritten && isSame
        } ?: return Checked(
            value = null,
            line = item,
            problem = "${Arithmetic.written(value = value)} is not written on such a line of the notes read."
        )

        return Checked(
            value = value,
            line = line.take(n = LINE),
            problem = ""
        )
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
