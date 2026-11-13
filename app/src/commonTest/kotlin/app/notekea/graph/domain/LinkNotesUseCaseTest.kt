package app.notekea.graph.domain

import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Linking two notes from the graph. */
class LinkNotesUseCaseTest {
    private val store = FakeNoteContentStore()
    private val linkNotes = LinkNotesUseCase(
        noteContentStore = store,
        applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)
    )

    @Test
    fun `the link goes after the body, and the frontmatter stays`() = runTest {
        store.texts[SOURCE] = "---\ntag: food\n---\nSome text.\n\n\n"

        linkNotes(VAULT, SOURCE, TARGET, listOf(SOURCE, TARGET))

        assertEquals(
            expected = "---\ntag: food\n---\nSome text.\n\n[[Salsa]]\n",
            actual = store.texts[SOURCE]
        )
    }

    @Test
    fun `an empty note gets only the link`() = runTest {
        store.texts[SOURCE] = ""

        linkNotes(VAULT, SOURCE, TARGET, listOf(SOURCE, TARGET))

        assertEquals(
            expected = "[[Salsa]]\n",
            actual = store.texts[SOURCE]
        )
    }

    @Test
    fun `two notes of the same name are told apart by the path`() = runTest {
        store.texts[SOURCE] = "Text"

        linkNotes(
            VAULT,
            SOURCE,
            TARGET,
            listOf(SOURCE, TARGET, RelativePath(value = "Other/Salsa.md"))
        )

        assertEquals(
            expected = "Text\n\n[[Sauces/Salsa]]\n",
            actual = store.texts[SOURCE]
        )
    }

    @Test
    fun `a note that cannot be read changes nothing`() = runTest {
        assertEquals(
            expected = null,
            actual = linkNotes(VAULT, SOURCE, TARGET, listOf(SOURCE, TARGET))
        )
    }

    private companion object {
        val SOURCE = RelativePath(value = "Recipes/Arepas.md")
        val TARGET = RelativePath(value = "Sauces/Salsa.md")
    }
}
