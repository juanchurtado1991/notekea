package app.notekea.editor.data

import com.ghost.serialization.Ghost
import kotlin.test.Test
import kotlin.test.assertEquals

/** YouTube's oEmbed answer, as a pasted link's title and picture come from it (#141). */
class OEmbedDtoTest {
    @Test
    fun `the title and the picture's address are read with their escapes undone, the other fields left`() {
        val json = """{"title":"Cómo hacer \"arepas\"","author_name":"Ana","height":113,""" +
            """"thumbnail_url":"https:\/\/i.ytimg.com\/vi\/dQw4w9WgXcQ\/hqdefault.jpg","provider_name":"YouTube"}"""

        assertEquals(
            expected = OEmbedDto(
                title = "Cómo hacer \"arepas\"",
                thumbnailUrl = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"
            ),
            actual = Ghost.deserialize<OEmbedDto>(json)
        )
    }

    @Test
    fun `an answer with neither is read as having none`() {
        assertEquals(
            expected = OEmbedDto(),
            actual = Ghost.deserialize<OEmbedDto>("""{"provider_name":"YouTube"}""")
        )
    }
}
