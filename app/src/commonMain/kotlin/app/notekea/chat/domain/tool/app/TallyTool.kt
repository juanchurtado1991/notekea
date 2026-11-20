package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult
import app.notekea.core.notes.domain.NoteTextCache

/**
 * Figures from the rows of a note, worked out by the app (`docs/loop-goal-plan.md`, step 3.2): the model names the
 * rows [RowsTool] numbered, and the code reads their figures and says the rows it did not count, so a row left out
 * is seen and none is copied wrong.
 */
class TallyTool(private val noteTextCache: NoteTextCache) : ChatTool {
    override val name = NAME

    override val description =
        "figures from the rows of a note, worked out by the app: how (sum, average, count, product, min, max or " +
            "difference), note, rows (the ids rows gave, as R1, R4, or all), column (the column or the word the " +
            "figure is under, when a row writes several). Call rows first. Never add up yourself."

    override val arguments = listOf(
        ToolArgument(name = HOW),
        ToolArgument(name = NOTE),
        ToolArgument(
            name = ROWS,
            isList = true
        ),
        ToolArgument(
            name = COLUMN,
            isRequired = false
        )
    )

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = "${call.text(name = HOW)} of ${idsOf(call = call).joinToString(separator = " ")}"

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult {
        val how = Tally.entries.firstOrNull {
            it.written == call.text(name = HOW).lowercase()
        }
            ?: return ToolResult(forModel = "tally: how is one of ${Tally.entries.joinToString { it.written }}.")

        val named = call.text(name = NOTE)

        val (path, rows) = RowsTool.rowsOf(
            name = named,
            context = context,
            noteTextCache = noteTextCache
        ) ?: return ToolResult(forModel = "tally: no note \"$named\". Call rows with the note first.")

        val ids = idsOf(call = call)
        val column = call.text(name = COLUMN)

        val isAll = ids.any {
            it == ALL
        }

        val unknown = ids.filter { id ->
            id != ALL && rows.none { it.id == id }
        }

        val isUnknown = ids.isEmpty() || unknown.isNotEmpty()

        if (isUnknown) {
            return ToolResult(
                forModel = "tally: ${path.nameWithoutExtension} has rows R1 to R${rows.size}" +
                    unknown
                        .takeIf { it.isNotEmpty() }
                        ?.let { ", not ${it.joinToString()}" }
                        .orEmpty() +
                    ". Call rows with the note to see them, then tally them by id."
            )
        }

        val counted = if (isAll) {
            rows.filter {
                hasFigure(
                    row = it,
                    column = column
                )
            }
        } else {
            ids.distinct().mapNotNull { id ->
                rows.firstOrNull {
                    it.id == id
                }
            }
        }

        return ToolResult(
            forModel = talliedOf(
                how = how,
                counted = counted,
                rows = rows,
                column = column
            )
        )
    }

    // The result with the rows counted and those of the note left out, or why a row has no figure to count.
    private fun talliedOf(
        how: Tally,
        counted: List<NoteRows.Row>,
        rows: List<NoteRows.Row>,
        column: String
    ): String {
        val figures = counted.map {
            it to NoteRows.figureOf(
                row = it,
                column = column
            )
        }

        val refused = figures.filter { (_, figure) ->
            figure.value == null
        }

        val isRefused = refused.isNotEmpty() || figures.isEmpty()

        if (isRefused) {
            val under = if (column.isNotEmpty()) " under $column" else ""

            val problems = refused
                .joinToString(separator = " ") { (_, figure) -> figure.problem }
                .ifEmpty { "No row of it writes a figure$under." }

            return "tally: not worked out. $problems"
        }

        val values = figures.mapNotNull { (_, figure) -> figure.value }
        val result = how.of(values = values)

        val shown = figures
            .joinToString(separator = "\n") { (row, figure) ->
                "- ${Arithmetic.written(value = figure.value ?: 0.0)}: ${NoteRows.shownOf(row = row).take(n = LINE)}"
            }

        val left = rows
            .filter { row ->
                val isCounted = counted.any {
                    it.id == row.id
                }

                !isCounted && hasFigure(
                    row = row,
                    column = column
                )
            }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n",
                prefix = "\nNot counted:\n"
            ) { "- ${NoteRows.shownOf(row = it).take(n = LINE)}" }
            .orEmpty()

        return "tally ${how.written} of ${values.size}: ${Arithmetic.written(value = result)}\n" +
            "Counted:\n$shown$left"
    }

    private fun hasFigure(
        row: NoteRows.Row,
        column: String
    ): Boolean = NoteRows
        .figureOf(
            row = row,
            column = column
        ).value != null

    // The ids written, however the model wrote them: "R1, R4", "r2", "3", or all.
    private fun idsOf(call: ToolCall): List<String> = call
        .list(name = ROWS)
        .flatMap { it.split(',', ' ', ';') }
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }
        .map { id ->
            when {
                id == ALL -> ALL
                id.all { it.isDigit() } -> "R$id"
                else -> id
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
        const val NOTE = "note"
        const val ROWS = "rows"
        const val COLUMN = "column"
        private const val ALL = "ALL"
        private const val LINE = 140
    }
}
