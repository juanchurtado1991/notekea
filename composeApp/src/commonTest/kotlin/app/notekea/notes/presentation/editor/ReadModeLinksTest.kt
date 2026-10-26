package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.LinkKind
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadModeLinksTest {
    private val links = ReadModeLinks()

    @Test
    fun `wiki links become links to their note, shown with their alias or as written`() {
        assertEquals(
            expected = "Cook [Arepas](notekea:Arepas), [the arepas](notekea:Recipes/Arepas) and " +
                "[Arepas#Dough](notekea:Arepas), see [Café con leche](notekea:Caf%C3%A9%20con%20leche).",
            actual = links.prepare(
                body = "Cook [[Arepas]], [[Recipes/Arepas|the arepas]] and [[Arepas#Dough]], see ![[Café con leche]]."
            )
        )
    }

    @Test
    fun `code and links to a heading of the same note are left as they are`() {
        val body = "Inline `[[not a link]]` and [[#Dough]]\n```\n[[Arepas]]\n```\n[[Arepas]]"

        assertEquals(
            expected = "Inline `[[not a link]]` and [[#Dough]]\n```\n[[Arepas]]\n```\n[Arepas](notekea:Arepas)",
            actual = links.prepare(body = body)
        )
    }

    @Test
    fun `tags become links that search them, nested and accented tags too`() {
        assertEquals(
            expected = "Dinner [#recipe](notekea-tag:recipe) and [#café/morning](notekea-tag:caf%C3%A9/morning).",
            actual = links.prepare(body = "Dinner #recipe and #café/morning.")
        )
    }

    @Test
    fun `tags inside code, links and web addresses, headings and numbers stay as they are`() {
        val body = "`#code` [#label](https://a.com) [[Note#Part]] <https://a.com/#x> https://a.com/#y\n" +
            "# Heading\n#2026 and issue#5\n```\n#fenced\n```"

        assertEquals(
            expected = "`#code` [#label](https://a.com) [Note#Part](notekea:Note) <https://a.com/#x> " +
                "https://a.com/#y\n# Heading\n#2026 and issue#5\n```\n#fenced\n```",
            actual = links.prepare(body = body)
        )
    }

    @Test
    fun `a clicked link is a note, a note path or an external address`() {
        assertEquals(
            expected = listOf(
                ReadLink.Tag(name = "café/morning"),
                ReadLink.Note(target = "Café con leche", kind = LinkKind.WIKI),
                ReadLink.Note(target = "Recipes/Arepas%20reina.md", kind = LinkKind.MARKDOWN_ENCODED),
                ReadLink.External(uri = "https://example.com/a#b"),
                ReadLink.External(uri = "mailto:ana@example.com"),
                null
            ),
            actual = listOf(
                "notekea-tag:caf%C3%A9/morning",
                "notekea:Caf%C3%A9%20con%20leche",
                "Recipes/Arepas%20reina.md#Dough",
                "https://example.com/a#b",
                "mailto:ana@example.com",
                "#Dough"
            ).map { links.classify(uri = it) }
        )
    }

    @Test
    fun `files embedded in a paragraph or a table cell become images with their width, or links that open them`() {
        assertEquals(
            expected = "| Arepas | ![Arepas plate.png](notekea-file:Arepas%20plate.png?width=80) |\n" +
                "See ![img/a.png](notekea-file:img/a.png) and [the menu](notekea-file:Weekly%20menu.pdf).",
            actual = links.prepare(
                body = "| Arepas | ![[Arepas plate.png\\|80]] |\nSee ![[img/a.png]] and ![[Weekly menu.pdf|the menu]]."
            )
        )
        assertEquals(
            expected = listOf("Arepas plate.png" to 80, "Weekly menu.pdf" to null),
            actual = listOf("notekea-file:Arepas%20plate.png?width=80", "notekea-file:Weekly%20menu.pdf").map {
                links.fileOf(address = it)
            }
        )
        assertEquals(
            expected = ReadLink.File(target = "Arepas plate.png"),
            actual = links.classify(uri = "notekea-file:Arepas%20plate.png?width=80")
        )
    }
}
