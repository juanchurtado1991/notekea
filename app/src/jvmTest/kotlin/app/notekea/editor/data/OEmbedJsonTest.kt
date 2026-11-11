package app.notekea.editor.data

import kotlin.test.Test
import kotlin.test.assertEquals

/** YouTube's oEmbed answer, as a pasted link's title and picture come from it (#141). */
class OEmbedJsonTest {
    @Test
    fun `the title and the picture's address are read with their escapes undone`() {
        val json = """{"title":"Cómo hacer \"arepas\"","author_name":"Ana",""" +
            """"thumbnail_url":"https:\/\/i.ytimg.com\/vi\/dQw4w9WgXcQ\/hqdefault.jpg","provider_name":"YouTube"}"""

        assertEquals(
            expected = listOf("Cómo hacer \"arepas\"", "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", null),
            actual = listOf("title", "thumbnail_url", "missing").map {
                OEmbedJson.stringOf(
                    json = json,
                    name = it
                )
            }
        )
    }
}
