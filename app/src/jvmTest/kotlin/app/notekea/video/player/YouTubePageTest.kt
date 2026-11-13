package app.notekea.video.player

import kotlin.test.Test
import kotlin.test.assertEquals

/** The page of the YouTube tab, and where its player sits in the window. */
class YouTubePageTest {
    @Test
    fun `the page plays the video from the app's own origin, and an id YouTube never gives makes no page`() {
        val page = YouTubePage.of(id = "dQw4w9WgXcQ").orEmpty()

        assertEquals(
            expected = listOf<Any?>(true, true, true, null, null),
            actual = listOf(
                "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ?autoplay=1" in page,
                "origin=https://app.notekea" in page,
                "allowfullscreen" in page,
                YouTubePage.of(id = "\"><script>alert(1)</script>"),
                YouTubePage.of(id = "")
            )
        )
    }

    @Test
    fun `the player's place is in points, whatever the screen's scale, counted from the bottom for AppKit`() {
        val place = PlayerPlace.of(
            left = 800f,
            top = 100f,
            width = 1600f,
            height = 900f,
            scale = 2.0
        )

        assertEquals(
            expected = listOf<Any>(
                PlayerPlace(
                    left = 400.0,
                    top = 50.0,
                    width = 800.0,
                    height = 450.0
                ),
                300.0,
                50.0
            ),
            actual = listOf(
                place,
                PlayerPlace.yOf(
                    top = place.top,
                    height = place.height,
                    contentHeight = 800.0,
                    isFlipped = false
                ),
                PlayerPlace.yOf(
                    top = place.top,
                    height = place.height,
                    contentHeight = 800.0,
                    isFlipped = true
                )
            )
        )
    }
}
