package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.EmbedKind
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.RelativePath
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
}
