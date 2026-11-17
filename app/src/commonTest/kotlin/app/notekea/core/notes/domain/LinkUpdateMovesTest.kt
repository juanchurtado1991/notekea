package app.notekea.core.notes.domain

import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.links.NoteRewrite
import app.notekea.core.notes.domain.links.PlanLinkUpdatesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Link updates found by the bug hunt of 2026-11-17: moved notes, attachments and names a wiki link cannot hold. */
class LinkUpdateMovesTest {
    private val store = FakeNoteContentStore()
    private val planLinkUpdates = PlanLinkUpdatesUseCase(
        noteContentStore = store,
        linkFinder = JetBrainsMarkdownLinkFinder()
    )

    // A4: a moved note's relative links kept pointing from its old folder, and broke.
    @Test
    fun `a moved note's own relative links are rewritten from its new folder, the rest left as written`() =
        runTest {
            store.texts[path(MOVED)] = "[x](../Recipes/Arepas.md) and [[./Plan]] and [[Arepas]]"

            val rewrites = planLinkUpdates(
                vaultPath = VAULT,
                notesBefore = setOf(path(DAY), path("Journal/Plan.md"), path(AREPAS)),
                from = path(DAY),
                to = path(MOVED)
            )

            assertEquals(
                expected = "[x](../../Recipes/Arepas.md) and [[../../Journal/Plan]] and [[Arepas]]",
                actual = rewrites.single().updated
            )
        }

    // A5: links and embeds to a renamed picture were left pointing at the old name.
    @Test
    fun `links and embeds to a renamed attachment follow it`() = runTest {
        store.texts[path(WELCOME)] = "![[pic.png]] ![](assets/pic.png) ![[assets/pic.png|200]]"

        val rewrites = planLinkUpdates(
            vaultPath = VAULT,
            notesBefore = setOf(path(WELCOME)),
            from = path("assets/pic.png"),
            to = path("assets/photo.png")
        )

        assertEquals(
            expected = "![[photo.png]] ![](assets/photo.png) ![[assets/photo.png|200]]",
            actual = rewrites.single().updated
        )
    }

    // G4: `[[C# arepas]]` was written, which leads to the heading "arepas" of a note "C".
    @Test
    fun `a wiki link to a note renamed with a hash becomes a Markdown link, keeping its embed, heading and alias`() =
        runTest {
            val rewrites = renameArepas(
                to = "Recipes/C# arepas.md",
                text = "[[Arepas]] and ![[Arepas#Dough|the dough]]"
            )

            assertEquals(
                expected = "[C# arepas](Recipes/C%23%20arepas.md) and " +
                    "![the dough](Recipes/C%23%20arepas.md#Dough)",
                actual = rewrites.single().updated
            )
        }

    // G5 and G7: a link with no extension was not found, and a leading slash was lost.
    @Test
    fun `a Markdown link with no extension or a leading slash keeps its form`() = runTest {
        val rewrites = renameArepas(
            to = "Recipes/Arepas reina.md",
            text = "[x](Recipes/Arepas) [y](/Recipes/Arepas.md)"
        )

        assertEquals(
            expected = "[x](Recipes/Arepas%20reina) [y](/Recipes/Arepas%20reina.md)",
            actual = rewrites.single().updated
        )
    }

    private suspend fun renameArepas(
        to: String,
        text: String
    ): List<NoteRewrite> {
        store.texts[path(WELCOME)] = text

        return planLinkUpdates(
            vaultPath = VAULT,
            notesBefore = setOf(path(AREPAS), path(WELCOME)),
            from = path(AREPAS),
            to = path(to)
        )
    }

    private fun path(value: String): RelativePath = RelativePath(value = value)

    private companion object {
        const val AREPAS = "Recipes/Arepas.md"
        const val DAY = "Journal/Day.md"
        const val MOVED = "Archive/2026/Day.md"
        const val WELCOME = "Welcome.md"
    }
}
