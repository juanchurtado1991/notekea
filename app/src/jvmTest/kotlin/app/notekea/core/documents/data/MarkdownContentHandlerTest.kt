package app.notekea.core.documents.data

import app.notekea.core.documents.domain.DocumentFormat
import org.xml.sax.SAXException
import org.xml.sax.helpers.AttributesImpl
import kotlin.test.Test
import kotlin.test.assertEquals

/** Tika's XHTML as Markdown: a long document is read only up to the limit, its pages within what was read. */
class MarkdownContentHandlerTest {
    // Code review of 2026-11-11: a long document was held whole, then cut, leaving pages past the text's end.
    @Test
    fun `a long document stops at the limit, and its pages end within the text`() {
        val handler = MarkdownContentHandler(
            format = DocumentFormat.PDF,
            maxChars = LIMIT
        )

        val page = AttributesImpl().apply { addAttribute("", "class", "class", "CDATA", "page") }
        val line = "A line of the manual that fills the page.".toCharArray()

        val stopped = runCatching {
            repeat(times = PAGES) {
                handler.startElement("", "div", "div", page)
                handler.startElement("", "p", "p", AttributesImpl())
                handler.characters(line, 0, line.size)
                handler.endElement("", "p", "p")
                handler.endElement("", "div", "div")
            }
        }.exceptionOrNull()

        val result = handler.result()

        assertEquals(
            expected = listOf<Any?>(true, true, true, true),
            actual = listOf(
                stopped is SAXException,
                handler.isFull,
                result.text.length <= LIMIT + line.size,
                result.parts.all { it.range.last < result.text.length } && result.parts.size < PAGES
            )
        )
    }

    private companion object {
        const val LIMIT = 300
        const val PAGES = 50
    }
}
