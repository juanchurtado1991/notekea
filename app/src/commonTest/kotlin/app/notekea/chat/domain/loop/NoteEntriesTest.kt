package app.notekea.chat.domain.loop

import kotlin.test.Test
import kotlin.test.assertEquals

/** The entries of a note's text: what they are, and the section each is in. */
class NoteEntriesTest {
    @Test
    fun `items, tasks and rows are entries of their section, and a table's heading row and its rule are not`() {
        val text = """
            # Home
            A line of text.
            1. Call the bank

            ## Bills
            | Bill | Paid |
            |---|---|
            | Water | no |
            - [ ] Pay the rent
            * Keep the receipts
        """.trimIndent()

        assertEquals(
            expected = listOf(
                listOf("1. Call the bank", NoteEntryKind.ITEM, "# Home", 1),
                listOf("| Water | no |", NoteEntryKind.ROW, "## Bills", 2),
                listOf("- [ ] Pay the rent", NoteEntryKind.TASK, "## Bills", 2),
                listOf("* Keep the receipts", NoteEntryKind.ITEM, "## Bills", 2)
            ),
            actual = NoteEntries
                .of(text = text)
                .map { listOf(it.line, it.kind, it.heading, it.section) }
        )
    }
}
