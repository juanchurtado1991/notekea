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
    fun `a clicked link is a note, a note path or an external address`() {
        assertEquals(
            expected = listOf(
                ReadLink.Note(target = "Café con leche", kind = LinkKind.WIKI),
                ReadLink.Note(target = "Recipes/Arepas%20reina.md", kind = LinkKind.MARKDOWN_ENCODED),
                ReadLink.External(uri = "https://example.com/a#b"),
                ReadLink.External(uri = "mailto:ana@example.com"),
                null
            ),
            actual = listOf(
                "notekea:Caf%C3%A9%20con%20leche",
                "Recipes/Arepas%20reina.md#Dough",
                "https://example.com/a#b",
                "mailto:ana@example.com",
                "#Dough"
            ).map { links.classify(uri = it) }
        )
    }
}
