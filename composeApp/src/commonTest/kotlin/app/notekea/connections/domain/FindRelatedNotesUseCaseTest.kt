package app.notekea.connections.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.search.domain.FakeIndexStateStore
import app.notekea.search.domain.IndexVaultUseCase
import app.notekea.search.domain.PlanIndexUseCase

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

    // Audit: the note a click opens was missing from "Already linked" when another note shared its name.
    @Test
    fun `a link to a name two notes share counts as linking the note a click opens`() = runTest {
        val root = RelativePath(value = "Arepas.md")
        val shared = RelativePath(value = "Recipes/Arepas.md")

        val result =
            findRelated(vaultPath = VAULT, note = GARDEN, text = "See [[Arepas]].", notes = ALL + root + shared)

        assertEquals(expected = listOf(root), actual = (result as Outcome.Success).value.linked)
    }

    // Audit: a note the model could not read showed as the search model failing to load.
    @Test
    fun `a note too long for the model is told apart from a failing model, its links still shown`() = runTest {
        embedder.failure = EmbeddingError.TextTooLong(index = 0, tokens = 600)

        val result = findRelated(vaultPath = VAULT, note = GARDEN, text = "See [[Weekly menu]].", notes = ALL)

        assertEquals(
            expected = Outcome.Success(
                value = RelatedNotes(related = emptyList(), linked = listOf(MENU), isTooLong = true)
            ),
            actual = result
        )
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

        assertEquals(expected = Outcome.Failure(error = IndexRunError.Model), actual = model)
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
