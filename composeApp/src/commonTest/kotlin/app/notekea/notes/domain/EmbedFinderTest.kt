package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class EmbedFinderTest {
    private val finder = EmbedFinder()

    @Test
    fun `wiki and markdown embeds of files are found with their width, caption and whether they fill their line`() {
        val text = """
            ![[Pasted image 20261026143005.png]]
            See ![[photo.jpg|300]] and ![[manual.pdf|The manual]].
            ![A plate|250](images/arepas%20plate.png)
            ![](<images/with space.png>)
        """.trimIndent()

        assertEquals(
            expected = listOf(
                Triple("Pasted image 20261026143005.png", null as Int?, true),
                Triple("photo.jpg", 300, false),
                Triple("manual.pdf", null, false),
                Triple("images/arepas plate.png", 250, true),
                Triple("images/with space.png", null, true)
            ),
            actual = finder.embeds(text = text).map { Triple(it.target, it.width, it.isOwnLine) }
        )
        assertEquals(
            expected = listOf(null, null, "The manual", "A plate", null),
            actual = finder.embeds(text = text).map { it.label }
        )
    }

    @Test
    fun `embeds of notes and anything in code are left out, and ranges point at the embed in the text`() {
        val text = "![[Other note]] ![[Recipe.md]]\n`![[inline.png]]`\n```\n![[fenced.png]]\n```\nEnd ![[end.png]]"
        val embeds = finder.embeds(text = text)

        assertEquals(
            expected = listOf("![[end.png]]"),
            actual = embeds.map { text.substring(range = it.range) }
        )
    }

    @Test
    fun `an embed in a table cell, its pipe escaped, keeps its file and width`() {
        val embeds = finder.embeds(text = "| Dish | Photo |\n| --- | --- |\n| Arepas | ![[Arepas plate.png\\|80]] |")

        assertEquals(
            expected = listOf(Triple("Arepas plate.png", 80 as Int?, false)),
            actual = embeds.map { Triple(it.target, it.width, it.isOwnLine) }
        )
    }
}
