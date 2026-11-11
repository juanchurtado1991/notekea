package app.notekea.documents.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

/** A document's text as the viewer's blocks: headings by level, table rows without their rule, ranges kept. */
class DocumentBlockTest {
    @Test
    fun `headings, paragraphs and table rows keep the range of the text they cover`() {
        val text = "## Page 1\n\nTrams climb.\n\n| Item | Cost |\n| --- | --- |\n| Pass | 40 |\n"
        val blocks = DocumentBlock.of(text = text)

        assertEquals(
            expected = listOf(
                Triple("Page 1", 2, false),
                Triple("Trams climb.", 0, false),
                Triple("Item   Cost", 0, true),
                Triple("Pass   40", 0, true)
            ) to "Trams climb.",
            actual = blocks.map {
                Triple(it.text, it.level, it.isTableRow)
            } to text.substring(blocks[1].range)
        )
    }
}
