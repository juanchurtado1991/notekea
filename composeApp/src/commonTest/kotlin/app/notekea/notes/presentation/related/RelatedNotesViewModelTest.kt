package app.notekea.notes.presentation.related

import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.ChunkNoteUseCase
import app.notekea.notes.domain.FakeIndexStateStore
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeVectorIndex
import app.notekea.notes.domain.FindRelatedNotesUseCase
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.IndexVaultUseCase
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteSections
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.PlanIndexUseCase
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * The Related section (`ROADMAP.md` 2.4): it follows the open note, searches once the person pauses typing and when
 * the index ends a run, and tells a failing model apart.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RelatedNotesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val contents = FakeNoteContentStore()
    private val embedder = FakeTextEmbedder()
    private val index = FakeVectorIndex()
    private val chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = RelatedNotesViewModel(
        vaultPath = VAULT,
        findRelatedNotes = FindRelatedNotesUseCase(
            chunkNote = chunkNote,
            textEmbedder = embedder,
            vectorIndex = index,
            noteContentStore = contents,
            linkFinder = JetBrainsMarkdownLinkFinder()
        )
    )

    @Test
    fun `opening a note finds its related notes, and another note starts again`() = runTest(context = testDispatcher) {
        indexed()
        val viewModel = viewModel()
        viewModel.onIntent(intent = RelatedNotesIntent.OnNoteChange(text = "Tomatoes and basil.", note = GARDEN))
        advanceUntilIdle()
        val garden = viewModel.state.value.related.map { it.path }
        viewModel.onIntent(intent = RelatedNotesIntent.OnNoteChange(text = "Quartz counters.", note = KITCHEN))
        val whileLoading = viewModel.state.value.let { it.isLoading to it.related }

        assertEquals(expected = SOUP, actual = garden.first())
        assertEquals(expected = true to emptyList(), actual = whileLoading)
    }

    @Test
    fun `typing searches once the person pauses`() = runTest(context = testDispatcher) {
        indexed()
        val viewModel = viewModel()
        viewModel.onIntent(intent = RelatedNotesIntent.OnNoteChange(text = "Tomatoes", note = GARDEN))
        advanceUntilIdle()
        val before = embedder.embedded.size
        listOf("Tomatoes a", "Tomatoes an", "Tomatoes and basil").forEach {
            viewModel.onIntent(intent = RelatedNotesIntent.OnNoteChange(text = it, note = GARDEN))
            advanceTimeBy(delayTimeMillis = 300)
        }
        advanceUntilIdle()

        assertEquals(expected = 1, actual = embedder.embedded.size - before)
    }

    @Test
    fun `a search that fails unexpectedly says so instead of looking like nothing related`() =
        runTest(context = testDispatcher) {
            indexed()
            contents.onRead = { error("bug") }
            val viewModel = viewModel()

            viewModel.onIntent(intent = RelatedNotesIntent.OnNoteChange(text = "Tomatoes.", note = GARDEN))
            advanceUntilIdle()

            assertEquals(expected = IndexRunError.Index, actual = viewModel.state.value.error)
        }

    @Test
    fun `the end of an indexing run searches again, and a failing model is told`() = runTest(context = testDispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(intent = RelatedNotesIntent.OnNoteChange(text = "Tomatoes.", note = GARDEN))
        advanceUntilIdle()
        val beforeIndex = viewModel.state.value.let { it.isIndexReady to it.related }
        indexed()
        viewModel.onIntent(intent = RelatedNotesIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()
        val afterIndex = viewModel.state.value.related.isNotEmpty()
        embedder.failure = EmbeddingError.EngineFailed
        viewModel.onIntent(intent = RelatedNotesIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        assertEquals(
            expected = listOf<Any?>(false to emptyList<Any>(), true, IndexRunError.Model),
            actual = listOf(beforeIndex, afterIndex, viewModel.state.value.error)
        )
    }

    @Test
    fun `already linked opens and folds`() = runTest(context = testDispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(intent = RelatedNotesIntent.OnLinkedToggle)

        assertEquals(expected = true, actual = viewModel.state.value.isLinkedOpen)
    }

    private suspend fun indexed() {
        val notes = listOf(
            SOUP to "Ripe tomatoes roasted with basil.",
            KITCHEN to "Quartz counters and a sink.",
            GARDEN to "Tomatoes and basil grow in the beds."
        ).map { (path, text) ->
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
        )(vaultPath = VAULT, notes = notes).last()
    }

    private companion object {
        const val VAULT = "/home/ana/Notes"
        val DATE = Instant.parse(input = "2026-11-03T10:00:00Z")
        val SOUP = RelativePath(value = "Recipes/Tomato soup.md")
        val KITCHEN = RelativePath(value = "Projects/Kitchen.md")
        val GARDEN = RelativePath(value = "Projects/Garden.md")
    }
}
