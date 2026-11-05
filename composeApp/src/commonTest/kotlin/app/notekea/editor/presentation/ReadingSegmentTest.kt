package app.notekea.editor.presentation

import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.editor.domain.Embed
import app.notekea.editor.domain.EmbedKind
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingSegmentTest {
    @Test
    fun `read mode cuts at files on their own line and before each heading, which its piece knows`() {
        val text = "Intro\n# One\nBody\n![[a.png]]\n## Two\nEnd\n"
        val start = text.indexOf(string = "![[")
        val view = AttachmentView(
            embed = Embed(range = start..start + 9, target = "a.png", kind = EmbedKind.WIKI, isOwnLine = true),
            kind = AttachmentKind.IMAGE,
            path = RelativePath(value = "a.png")
        )

        assertEquals(
            expected = listOf(
                ReadingSegment.Markdown(text = "Intro\n"),
                ReadingSegment.Markdown(text = "# One\nBody\n", heading = 0),
                ReadingSegment.Attachment(view = view),
                ReadingSegment.Markdown(text = "## Two\nEnd\n", heading = 1)
            ),
            actual = ReadingSegment.of(
                text = text,
                attachments = listOf(view),
                headings = OutlineFinder().headings(text = text)
            )
        )
    }


    @Test
    fun `a query block is cut out with its YAML, between the Markdown around it`() {
        val block = "```query\ncolumns: [rating]\n```"

        assertEquals(
            expected = listOf(
                ReadingSegment.Markdown(text = "Books\n\n"),
                ReadingSegment.Query(text = "columns: [rating]\n", source = block),
                ReadingSegment.Markdown(text = "\nThe end\n")
            ),
            actual = ReadingSegment.of(text = "Books\n\n$block\n\nThe end\n", attachments = emptyList())
        )
    }

    @Test
    fun `an empty query block and one ending the note are blocks too`() {
        assertEquals(
            expected = listOf(ReadingSegment.Query(text = "", source = "```query\n```")),
            actual = ReadingSegment.of(text = "```query\n```", attachments = emptyList())
        )
    }

    @Test
    fun `a query shown as code inside another block, or never closed, stays Markdown`() {
        val quoted = "````\n```query\ncolumns: [a]\n```\n````\n"
        val open = "```query\ncolumns: [a]\n"
        val other = "```kotlin\nval query = 1\n```\n"

        assertEquals(
            expected = listOf(quoted, open, other).map { listOf(ReadingSegment.Markdown(text = it)) },
            actual = listOf(quoted, open, other).map { ReadingSegment.of(text = it, attachments = emptyList()) }
        )
    }
}
