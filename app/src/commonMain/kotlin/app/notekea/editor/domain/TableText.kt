package app.notekea.editor.domain

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.markdown.TableAlignment

/**
 * A block of the note that is one table and nothing else, so it can be edited by cells: [rows] has the header first
 * (the delimiter row only gives the [alignments]), and the table takes [start] until [end] of the note. [afterStart]
 * is where text can be written under it, if the block has a second blank line.
 */
class TableText private constructor(
    val start: Int,
    val end: Int,
    val alignments: List<TableAlignment>,
    val rows: List<List<TableCell>>,
    val afterStart: Int?
) {
    val lastCell: TableCell
        get() = rows
            .last()
            .last()

    fun cell(
        row: Int,
        column: Int
    ): TableCell? = rows
        .getOrNull(index = row)
        ?.getOrNull(index = column)

    /** The cell [offset] is in; for an offset in a pipe, the delimiter row or under the table, the nearest one. */
    fun cellAt(offset: Int): TableCell = rows
        .flatten()
        .minBy { maxOf(it.start - offset, offset - it.end, 0) }

    companion object {
        private const val ESCAPE = '\\'
        private const val FILE_EMBED = "![["
        private const val NEW_LINE = '\n'
        private const val PADDING = ' '
        private const val PIPE = '|'
        private val WIKI_LINK = Regex(pattern = "\\[\\[[^]]*]]")

        /**
         * The table that [text] is, a block as the editor shows it, [offset] into the note; null unless every line is
         * a row between pipes with the header's number of cells. An alias pipe in a link (`[[a|b]]`) is not a cell's.
         */
        fun of(
            text: CharSequence,
            offset: Int = 0
        ): TableText? {
            if (!text.startsWith(prefix = MarkdownSyntax.TABLE_COLUMN)) return null

            val content = text.trimEnd()
            val lines = content.lines()

            if (lines.size < 2 || FILE_EMBED in content) return null

            var lineStart = 0

            val raw = lines.map { line ->
                rawCells(
                    line = line,
                    lineStart = lineStart
                ).also { lineStart += line.length + 1 } ?: return null
            }

            val alignments = raw[1].map {
                TableAlignment.of(delimiterCell = text.substring(range = it)) ?: return null
            }

            if (raw.any { it.size != raw[0].size }) return null

            val rows = (raw.take(n = 1) + raw.drop(n = 2)).mapIndexed { row, cells ->
                cells.mapIndexed { column, range ->
                    cellOf(
                        text = text,
                        raw = range,
                        offset = offset
                    ).copy(
                        row = row,
                        column = column
                    )
                }
            }

            val after = content.length + 2

            return TableText(
                start = offset,
                end = offset + content.length,
                alignments = alignments,
                rows = rows,
                afterStart = (offset + after).takeIf {
                    text.length >= after && text.endsWithBlank(end = after)
                }
            )
        }

        private fun CharSequence.endsWithBlank(end: Int): Boolean =
            this[end - 1] == NEW_LINE && this[end - 2] == NEW_LINE

        /** What is between the pipes of [line], which starts [lineStart] into the text; null if it is not a row. */
        private fun rawCells(
            line: String,
            lineStart: Int
        ): List<IntRange>? {
            val row = line.trimEnd()

            val pipes = row.indices.filter {
                row[it] == PIPE && row.getOrNull(index = it - 1) != ESCAPE
            }

            val isRow =
                pipes.size >= 2 &&
                    pipes.first() == 0 &&
                    pipes.last() == row.lastIndex

            val hasLinkPipe = WIKI_LINK.findAll(input = row).any { link ->
                pipes.any {
                    it in link.range
                }
            }

            if (!isRow || hasLinkPipe) return null

            return pipes.zipWithNext { open, close ->
                (lineStart + open + 1) until (lineStart + close)
            }
        }

        /** The cell of the [raw] text between two pipes: its text starts after the spaces, and ends before one. */
        private fun cellOf(
            text: CharSequence,
            raw: IntRange,
            offset: Int
        ): TableCell {
            val rawEnd = raw.last + 1
            val hasPadding = rawEnd > raw.first && text[rawEnd - 1] == PADDING
            val end = if (hasPadding) rawEnd - 1 else rawEnd

            val start = raw.firstOrNull {
                !text[it].isWhitespace()
            } ?: minOf(raw.first + 1, rawEnd)

            return TableCell(
                row = 0,
                column = 0,
                start = offset + start,
                end = offset + maxOf(start, end)
            )
        }
    }
}
