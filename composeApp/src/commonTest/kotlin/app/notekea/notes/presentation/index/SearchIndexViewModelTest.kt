package app.notekea.notes.presentation.index

import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.DescribeElapsedTimeUseCase
import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.notes.domain.ChunkNoteUseCase
import app.notekea.notes.domain.FakeIndexStateStore
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeVectorIndex
import app.notekea.notes.domain.IndexError
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.IndexVaultUseCase
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteSections
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.PlanIndexUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VectorIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The vault's search index while it is open (`ROADMAP.md` 2.3): a change of its notes starts one run a moment later,
 * a busy index is tried again, Rebuild index starts over, and a large first run shows its notice.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchIndexViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val contents = FakeNoteContentStore()
    private val index = FakeVectorIndex()
    private val states = FakeIndexStateStore()
    private val clock = FakeClock(currentTime = NOW)
    private val embedder = FakeTextEmbedder()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        vectorIndex: VectorIndex = index
    ) = SearchIndexViewModel(
        vaultPath = VAULT,
        indexVault = IndexVaultUseCase(
            planIndex = PlanIndexUseCase(),
            chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder())),
            noteContentStore = contents,
            textEmbedder = embedder,
            vectorIndex = vectorIndex,
            indexStateStore = states
        ),
        vectorIndex = vectorIndex,
        indexStateStore = states,
        describeElapsedTime = DescribeElapsedTimeUseCase(clock = clock),
        clock = clock
    )

    @Test
    fun `a burst of changes once the tree is loaded starts one run, which ends with the notes ready`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = viewModel()
        val notes = write(count = 2)
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = notes, isLoaded = false))
        advanceUntilIdle()
        val beforeLoaded = index.puts.size
        repeat(times = 3) {
            viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = notes, isLoaded = true))
        }
        advanceUntilIdle()

        assertEquals(expected = 0 to 2, actual = beforeLoaded to index.puts.size)
        assertEquals(expected = false to 2, actual = viewModel.state.value.let { it.isRunning to it.readyNotes })
    }

    @Test
    fun `an index another Notekea holds is tried again later`() = runTest(context = testDispatcher) {
        val viewModel = viewModel()
        index.isBusy = true
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = write(count = 1), isLoaded = true))
        advanceTimeBy(delayTimeMillis = 1_000)
        val whileBusy = index.puts.size
        index.isBusy = false
        advanceUntilIdle()

        assertEquals(expected = 0 to 1, actual = whileBusy to index.puts.size)
    }

    @Test
    fun `Rebuild index forgets the index and reads every note again`() = runTest(context = testDispatcher) {
        val viewModel = viewModel()
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = write(count = 2), isLoaded = true))
        advanceUntilIdle()
        index.puts.clear()

        viewModel.onIntent(intent = SearchIndexIntent.OnRebuildClick)
        advanceUntilIdle()

        assertEquals(expected = 2, actual = index.puts.size)
    }

    // Audit: a change of the tree cancelled Rebuild between emptying the index and forgetting the state, which went on
    // saying every note was done: the index stayed empty.
    @Test
    fun `a change of the notes in the middle of Rebuild still leaves every note in the index`() = runTest(
        context = testDispatcher
    ) {
        val slowToClear = object : VectorIndex by index {
            override suspend fun clear(
                vaultPath: String
            ): Outcome<Unit, IndexError> = index.clear(vaultPath = vaultPath).also { delay(timeMillis = 100) }
        }
        val viewModel = viewModel(vectorIndex = slowToClear)
        val notes = write(count = 2)
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = notes, isLoaded = true))
        advanceUntilIdle()

        viewModel.onIntent(intent = SearchIndexIntent.OnRebuildClick)
        advanceTimeBy(delayTimeMillis = 50)
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = notes, isLoaded = true))
        advanceUntilIdle()

        assertEquals(expected = 2, actual = index.notes.getValue(VAULT).size)
    }

    @Test
    fun `a Rebuild that cannot empty the index stops there and says so`() = runTest(context = testDispatcher) {
        val busyToClear = object : VectorIndex by index {
            override suspend fun clear(
                vaultPath: String
            ): Outcome<Unit, IndexError> = Outcome.Failure(error = IndexError.Busy)
        }
        val viewModel = viewModel(vectorIndex = busyToClear)
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = write(count = 2), isLoaded = true))
        advanceUntilIdle()
        index.puts.clear()

        viewModel.onIntent(intent = SearchIndexIntent.OnRebuildClick)
        advanceUntilIdle()

        assertEquals(expected = 0 to IndexRunError.INDEX, actual = index.puts.size to viewModel.state.value.error)
    }

    @Test
    fun `a Rebuild whose state cannot be forgotten leaves the index as it was and says so`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = viewModel()
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = write(count = 2), isLoaded = true))
        advanceUntilIdle()
        states.isClearRefused = true

        viewModel.onIntent(intent = SearchIndexIntent.OnRebuildClick)
        advanceUntilIdle()

        assertEquals(
            expected = 2 to IndexRunError.Index,
            actual = index.notes.getValue(VAULT).size to viewModel.state.value.error
        )
    }

    @Test
    fun `a large first run shows its notice until dismissed, and the details tell when the last run was`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = viewModel()
        embedder.millisPerText = 10
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = write(count = 120), isLoaded = true))
        advanceTimeBy(delayTimeMillis = 400)
        val showsNotice = viewModel.state.value.showsNotice
        viewModel.onIntent(intent = SearchIndexIntent.OnNoticeDismiss)
        val afterDismiss = viewModel.state.value.showsNotice
        advanceUntilIdle()
        clock.currentTime = NOW + 5.minutes
        viewModel.onIntent(intent = SearchIndexIntent.OnDetailsToggle)

        assertEquals(
            expected = listOf<Any?>(true, false, ElapsedTime.Minutes(count = 5)),
            actual = listOf(showsNotice, afterDismiss, viewModel.state.value.lastRun)
        )
    }

    /** [count] notes written to the vault, as the tree reports them. */
    private fun write(count: Int): List<NoteFile> = (1..count).map { number ->
        val path = RelativePath(value = "Note $number.md")
        contents.texts[path] = "# Note $number\nSome words of note $number."
        NoteFile(path = path, size = 1, modifiedAt = NOW)
    }

    private companion object {
        const val VAULT = "/home/ana/Notes"
        val NOW = Instant.parse(input = "2026-11-03T10:00:00Z")
    }
}
