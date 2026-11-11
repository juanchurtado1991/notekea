package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A YouTube video's address in each form people paste, and what is not one (the developer's request, 2026-11-11). */
class YouTubeLinksTest {
    @Test
    fun `every form of a video's address gives its id, other addresses none`() {
        assertEquals(
            expected = listOf(ID, ID, ID, ID, ID, ID, ID, null, null, null, null),
            actual = listOf(
                "https://www.youtube.com/watch?v=$ID",
                "https://youtube.com/watch?feature=share&v=$ID&t=42",
                "https://youtu.be/$ID?si=abc",
                "https://m.youtube.com/shorts/$ID",
                "https://www.youtube-nocookie.com/embed/$ID",
                "https://www.youtube.com/live/$ID",
                "  https://music.youtube.com/watch?v=$ID  ",
                "https://www.youtube.com/@notekea",
                "https://www.youtube.com/playlist?list=PL123",
                "https://notyoutube.com/watch?v=$ID",
                "attachments/trip.mp4"
            ).map { YouTubeLinks.idOf(url = it) }
        )
    }

    private companion object {
        const val ID = "dQw4w9WgXcQ"
    }
}
