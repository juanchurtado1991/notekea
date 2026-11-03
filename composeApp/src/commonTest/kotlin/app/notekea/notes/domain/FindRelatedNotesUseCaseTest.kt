package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * The open note's related notes (`ROADMAP.md` 2.4): closest in meaning first, never itself nor a note it links, at
 * most eight, each with the passage that matched; the notes it links apart.
 */
class FindRelatedNotesUseCaseTest {
    private val contents = FakeNoteContentStore()
    private val embedder = FakeTextEmbedder()
    private val index = FakeVectorIndex()
    private val chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))
    private val findRelated = FindRelatedNotesUseCase(
        chunkNote = chunkNote,
        textEmbedder = embedder,
        vectorIndex = index,
        noteContentStore = contents,
        linkFinder = JetBrainsMarkdownLinkFinder()
    )

    @Test
    fun `the closest notes come first, without the open note nor those it links, each with its passage`() = runTest {
        indexed(
            SOUP to "Ripe tomatoes roasted with garlic and basil. Summer tomatoes from the beds.",
            GARDEN to "Tomatoes and basil grow in the beds by the fence.",
            BUDGET to "Quartz counters cost money; the kitchen waits.",
            MENU to "Monday arepas, Tuesday tomato soup."
        )
        val text = "# Garden\nTomatoes and basil in the beds. See [[Weekly menu]]."

        val result = (findRelated(vaultPath = VAULT, note = GARDEN, text = text, notes = ALL) as Outcome.Success).value

        assertEquals(expected = listOf(MENU), actual = result.linked)
        assertEquals(expected = SOUP, actual = result.related.first().path)
        assertEquals(expected = false, actual = result.related.any { it.path == GARDEN || it.path == MENU })
        assertEquals(expected = true, actual = "tomatoes" in result.related.first().snippet.text.lowercase())
    }

    @Test
    fun `at most eight notes are related`() = runTest {
        indexed(*(1..12).map { RelativePath(value = "Note $it.md") to "Tomatoes and basil, note $it." }.toTypedArray())

        val result = findRelated(vaultPath = VAULT, note = GARDEN, text = "Tomatoes and basil.", notes = ALL)

        assertEquals(expected = FindRelatedNotesUseCase.LIMIT, actual = (result as Outcome.Success).value.related.size)
    }

    @Test
    fun `a failing model is told, rather than no related notes`() = runTest {
        indexed(SOUP to "Tomatoes.")
        embedder.failure = EmbeddingError.EngineFailed
        val model = findRelated(vaultPath = VAULT, note = GARDEN, text = "Tomatoes.", notes = ALL)

        assertEquals(expected = Outcome.Failure(error = IndexRunError.MODEL), actual = model)
    }

    /** Writes the notes and indexes them as the app does. */
    private suspend fun indexed(
        vararg notes: Pair<RelativePath, String>
    ) {
        val files = notes.map { (path, text) ->
            contents.texts[path] = text
            NoteFile(path = path, size = text.length.toLong(), modifiedAt = DATE)
        }
        IndexVaultUseCase(
            planIndex = PlanIndexUseCase(),
            chunkNote = chunkNote,
            noteContentStore = contents,
            textEmbedder = embedder,
            vectorIndex = index,
            indexStateStore = FakeIndexStateStore()
        )(vaultPath = VAULT, notes = files).last()
    }

    private companion object {
        const val VAULT = "/home/ana/Notes"
        val DATE = Instant.parse(input = "2026-11-03T10:00:00Z")
        val SOUP = RelativePath(value = "Recipes/Tomato soup.md")
        val GARDEN = RelativePath(value = "Projects/Garden.md")
        val BUDGET = RelativePath(value = "Projects/Budget.md")
        val MENU = RelativePath(value = "Recipes/Weekly menu.md")
        val ALL = listOf(SOUP, GARDEN, BUDGET, MENU)
    }
}
