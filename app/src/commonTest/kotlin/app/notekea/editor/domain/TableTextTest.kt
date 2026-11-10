package app.notekea.editor.domain

import app.notekea.core.domain.markdown.TableAlignment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TableTextTest {
    @Test
    fun `a table's cells are its texts without the spaces around them, the header first`() {
        val text = "| Name | Qty |\n| :-- | --: |\n| Corn flour |2|\n|  | \\| |\n"

        val table = assertNotNull(
            actual = TableText.of(
                text = text,
                offset = OFFSET
            )
        )

        assertEquals(
            expected = listOf(listOf("Name", "Qty"), listOf("Corn flour", "2"), listOf("", "\\|")),
            actual = table.rows.map { row ->
                row.map {
                    text.substring(
                        startIndex = it.start - OFFSET,
                        endIndex = it.end - OFFSET
                    )
                }
            }
        )

        assertEquals(
            expected = listOf(TableAlignment.LEFT, TableAlignment.RIGHT),
            actual = table.alignments
        )

        assertEquals(
            expected = OFFSET to OFFSET + text.length - 1,
            actual = table.start to table.end
        )
    }

    @Test
    fun `an empty cell keeps the cursor between its two spaces, and a cell keeps one typed space at its end`() {
        val table = assertNotNull(actual = TableText.of(text = "|  | a  |\n| --- | --- |"))

        assertEquals(
            expected = listOf(2 to 2, 5 to 7),
            actual = table.rows[0].map {
                it.start to it.end
            }
        )
    }

    @Test
    fun `text that is not just a table of whole rows is not edited by cells`() {
        assertEquals(
            expected = List(size = NOT_TABLES.size) { null },
            actual = NOT_TABLES.map { TableText.of(text = it) }
        )
    }

    @Test
    fun `an offset outside the cells belongs to the nearest one`() {
        val text = "| a | b |\n| --- | --- |\n| c | d |\n\n"
        val table = assertNotNull(actual = TableText.of(text = text))

        assertEquals(
            expected = listOf(0 to 0, 0 to 0, 0 to 1, 0 to 1, 1 to 0, 1 to 1),
            actual = listOf(0, 2, 5, 12, 25, text.length).map {
                val cell = table.cellAt(offset = it)

                cell.row to cell.column
            }
        )
    }

    @Test
    fun `text can be written under a table only after a blank line`() {
        assertNull(actual = TableText.of(text = "| a |\n| --- |\n")?.afterStart)
        assertNull(actual = TableText.of(text = "| a |\n| --- |")?.afterStart)

        assertEquals(
            expected = OFFSET + 15,
            actual = TableText
                .of(
                    text = "| a |\n| --- |\n\n",
                    offset = OFFSET
                )
                ?.afterStart
        )
    }

    companion object {
        private const val OFFSET = 40

        private val NOT_TABLES = listOf(
            "Plain text",
            "| a | b |",
            "| a | b |\n| --- |",
            "| a | b |\n| --- | --- |\n| c |",
            "| a | b |\n| --- | --- |\n| c | d | e |",
            "a | b\n--- | ---",
            " | a |\n | --- |",
            "| a |\n| --- |\nText under it",
            "| a |\n| -x- |",
            "| [[Note|alias]] |\n| --- |",
            "| ![[image.png]] |\n| --- |"
        )
    }
}
