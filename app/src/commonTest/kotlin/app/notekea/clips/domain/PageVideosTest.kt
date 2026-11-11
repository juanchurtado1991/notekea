package app.notekea.clips.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A clipped page's YouTube videos become cards in its note (the developer's request of 2026-11-11). */
class PageVideosTest {
    @Test
    fun `YouTube players and lite-youtube are found with their titles, once each, other players are not`() {
        val html = """
            <iframe width="560" src="https://www.youtube.com/embed/$ID?si=x" title="How to make arepas"></iframe>
            <iframe src='//www.youtube-nocookie.com/embed/$ID'></iframe>
            <lite-youtube videoid="$OTHER" playlabel="Masa al punto"></lite-youtube>
            <iframe src="https://player.vimeo.com/video/123" title="Vimeo"></iframe>
        """.trimIndent()

        assertEquals(
            expected = listOf(
                PageVideo(
                    id = ID,
                    title = "How to make arepas"
                ),
                PageVideo(
                    id = OTHER,
                    title = "Masa al punto"
                )
            ),
            actual = PageVideos.of(html = html)
        )
    }

    @Test
    fun `the note lists the videos as cards above the original text`() {
        val clip = WebClip(
            url = "https://example.com/arepas",
            title = "Arepas",
            notes = "",
            article = Article(
                title = "Arepas",
                paragraphs = listOf("Mix the flour.")
            ),
            videos = listOf(
                PageVideo(
                    id = ID,
                    title = "How [to] make arepas"
                ),
                PageVideo(
                    id = OTHER,
                    title = null
                )
            )
        )

        val text = WebClipNote.textOf(
            clip = clip,
            today = "2026-11-11"
        )

        assertEquals(
            expected = "## Videos\n\n![How  to  make arepas](https://www.youtube.com/watch?v=$ID)\n\n" +
                "![](https://www.youtube.com/watch?v=$OTHER)\n\n## Original text",
            actual = text.substring(text.indexOf("## Videos"), text.indexOf("## Original text") + "## Original text".length)
        )
    }

    private companion object {
        const val ID = "dQw4w9WgXcQ"
        const val OTHER = "abcdefghijk"
    }
}
