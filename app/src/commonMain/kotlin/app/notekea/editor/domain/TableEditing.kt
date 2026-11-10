package app.notekea.editor.domain

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.markdown.TableAlignment
import app.notekea.core.notes.domain.TextEdit

/**
 * A table edited from the cell the cursor is in, one [TextEdit] per [TableAction]; null where the cursor is not in a
 * table that can be edited by cells ([TableText]). A change to its rows or columns writes the whole table again, with
 * one space around each cell; deleting its only row or column deletes it.
 */
class TableEditing {
    fun edit(
        text: String,
        cursor: Int,
        action: TableAction
    ): TextEdit? {
        val blocks = NoteBlocks.of(text = text)
        val block = blocks.at(offset = cursor.coerceIn(range = 0..text.length))
        val start = blocks.start(index = block)

        val table = TableText.of(
            text = text.substring(
                startIndex = start,
                endIndex = blocks.textEnd(window = block..block)
            ),
            offset = start
        ) ?: return null

        if (action == TableAction.AFTER) {
            return after(
                text = text,
                table = table,
                blockEnd = blocks.end(index = block)
            )
        }

        val cell = table.cellAt(offset = cursor)

        val grid = Grid(
            cells = table.rows.map { row ->
                row.map {
                    text
                        .substring(
                            startIndex = it.start,
                            endIndex = it.end
                        )
                        .trim()
                }
            },
            alignments = table.alignments,
            row = cell.row,
            column = cell.column
        )

        val changed = grid.after(action = action) ?: return TextEdit(
            start = table.start,
            end = table.end,
            replacement = "",
            selectionStart = table.start,
            selectionEnd = table.start
        )

        val written = changed.written()

        val target = TableText
            .of(
                text = written,
                offset = table.start
            )
            ?.cell(
                row = changed.row,
                column = changed.column
            ) ?: return null

        return TextEdit(
            start = table.start,
            end = table.end,
            replacement = written,
            selectionStart = target.end,
            selectionEnd = target.end
        )
    }

    /** One blank line under the [table], whatever was there until [blockEnd], and the cursor after it. */
    private fun after(
        text: String,
        table: TableText,
        blockEnd: Int
    ): TextEdit {
        val isReady = text.startsWith(
            prefix = BLANK_LINE,
            startIndex = table.end
        )

        val cursor = table.end + BLANK_LINE.length

        return TextEdit(
            start = if (isReady) cursor else table.end,
            end = if (isReady) cursor else blockEnd,
            replacement = if (isReady) "" else BLANK_LINE,
            selectionStart = cursor,
            selectionEnd = cursor
        )
    }

    /** A table's texts, [cells] with the header first, and the cell ([row], [column]) the cursor is in. */
    private data class Grid(
        val cells: List<List<String>>,
        val alignments: List<TableAlignment>,
        val row: Int,
        val column: Int
    ) {
        /** The table after [action], with the cursor's new cell; null when nothing is left of it. */
        fun after(action: TableAction): Grid? = when (action) {
            TableAction.ROW_ABOVE -> withRow(at = row)

            TableAction.ROW_BELOW -> withRow(at = row + 1)

            TableAction.NEXT_ROW -> withRow(at = row + 1).copy(column = 0)

            TableAction.COLUMN_LEFT -> withColumn(at = column)

            TableAction.COLUMN_RIGHT -> withColumn(at = column + 1)

            TableAction.ALIGN_LEFT -> aligned(alignment = TableAlignment.LEFT)

            TableAction.ALIGN_CENTER -> aligned(alignment = TableAlignment.CENTER)

            TableAction.ALIGN_RIGHT -> aligned(alignment = TableAlignment.RIGHT)

            TableAction.DELETE_ROW -> copy(
                cells = cells.without(index = row),
                row = minOf(row, cells.size - 2)
            ).takeIf {
                cells.size > 1
            }

            TableAction.DELETE_COLUMN -> copy(
                cells = cells.map { it.without(index = column) },
                alignments = alignments.without(index = column),
                column = minOf(column, alignments.size - 2)
            ).takeIf {
                alignments.size > 1
            }

            TableAction.AFTER -> this
        }

        fun written(): String {
            val lines = cells.take(n = 1) + listOf(alignments.map { it.mark }) + cells.drop(n = 1)

            return lines
                .joinToString(separator = "\n") {
                    it.joinToString(
                        separator = MarkdownSyntax.TABLE_CELL_SEPARATOR,
                        prefix = MarkdownSyntax.TABLE_ROW_START,
                        postfix = MarkdownSyntax.TABLE_ROW_END
                    )
                }
        }

        private fun withRow(at: Int): Grid = copy(
            cells = cells.with(
                index = at,
                item = List(size = alignments.size) { "" }
            ),
            row = at
        )

        private fun withColumn(at: Int): Grid = copy(
            cells = cells.map {
                it.with(
                    index = at,
                    item = ""
                )
            },
            alignments = alignments.with(
                index = at,
                item = TableAlignment.NONE
            ),
            column = at
        )

        /** The cursor's column lined up as [alignment] says; as it already is, it goes back to no alignment. */
        private fun aligned(alignment: TableAlignment): Grid = copy(
            alignments = alignments.mapIndexed { index, current ->
                when {
                    index != column -> current
                    current == alignment -> TableAlignment.NONE
                    else -> alignment
                }
            }
        )

        private fun <T> List<T>.with(
            index: Int,
            item: T
        ): List<T> = toMutableList().apply {
            add(
                index = index,
                element = item
            )
        }

        private fun <T> List<T>.without(index: Int): List<T> = filterIndexed { at, _ ->
            at != index
        }
    }

    companion object {
        private const val BLANK_LINE = "\n\n"
    }
}
