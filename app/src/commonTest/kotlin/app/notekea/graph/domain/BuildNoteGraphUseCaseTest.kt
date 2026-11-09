package app.notekea.graph.domain

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Links become the graph's edges: only those that reach another note, once a pair. */
class BuildNoteGraphUseCaseTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()

    @Test
    fun `each pair of linked notes is one edge, and each note counts its links`() = runTest(
        dispatcher
    ) {
        store.texts[AREPAS] = "[[Soup]] and [[Soup]] again, [[Arepas]] itself, and [[Nowhere]]."
        store.texts[SOUP] = "Back to [[Arepas]]."
        store.texts[LONE] = "No links."

        val build = BuildNoteGraphUseCase(
            noteTextCache = NoteTextCache(
                contentStore = store,
                textFolder = TextFolder()
            ),
            linkFinder = JetBrainsMarkdownLinkFinder(),
            dispatchers = FakeAppDispatchers(dispatcher = dispatcher)
        )

        val graph = build(
            VAULT,
            listOf(AREPAS, SOUP, LONE).map { entry(path = it.value) }
        )

        assertEquals(
            expected = listOf(GraphEdge.between(AREPAS, SOUP)),
            actual = graph.edges
        )

        assertEquals(
            expected = listOf(1, 1, 0),
            actual = graph.nodes.map { it.links }
        )
    }

    private companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val SOUP = RelativePath(value = "Soup.md")
        val LONE = RelativePath(value = "Lone.md")
    }
}
