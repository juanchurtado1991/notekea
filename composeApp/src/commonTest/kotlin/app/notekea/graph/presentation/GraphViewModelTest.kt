package app.notekea.graph.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.FindRelatedNotesUseCase
import app.notekea.core.notes.domain.FuzzyMatcher
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.embedChunks
import app.notekea.graph.domain.BuildNoteGraphUseCase
import app.notekea.graph.domain.GraphPreferences
import app.notekea.graph.domain.GraphPreferencesStore
import app.notekea.graph.domain.LinkNotesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The graph tab (`ROADMAP.md` 2.7) over the real graph and layout, on a vault of a few linked notes. */
@OptIn(ExperimentalCoroutinesApi::class)
class GraphViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val embedder = FakeTextEmbedder()
    private val index = FakeVectorIndex()
    private val chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))
    private val preferences = object : GraphPreferencesStore {
        var kept = GraphPreferences()

        override suspend fun read(vaultPath: String): GraphPreferences = kept

        override suspend fun write(
            vaultPath: String,
            preferences: GraphPreferences
        ) {
            kept = preferences
        }
    }
    private val viewModel by lazy {
        val folder = TextFolder()
        val dispatchers = FakeAppDispatchers(dispatcher = dispatcher)
        val finder = JetBrainsMarkdownLinkFinder()
        val cache = NoteTextCache(contentStore = store, textFolder = folder)
        GraphViewModel(
            vaultPath = VAULT,
            buildNoteGraph = BuildNoteGraphUseCase(cache, finder, dispatchers),
            findRelatedNotes = FindRelatedNotesUseCase(
                chunkNote = chunkNote,
                textEmbedder = embedder,
                vectorIndex = index,
                noteContentStore = store,
                linkFinder = finder
            ),
            linkNotes = LinkNotesUseCase(store, ApplyLinkUpdatesUseCase(noteContentStore = store)),
            applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store),
            fuzzyMatcher = FuzzyMatcher(textFolder = folder),
            noteTextCache = cache,
            markdownLineStripper = MarkdownLineStripper(),
            preferencesStore = preferences,
            dispatchers = dispatchers
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
        store.texts[AREPAS] = "Mix the **flour**. See [[Menu]]."
        store.texts[MENU] = "Back to [[Arepas]] and [[Tasks]]."
        store.texts[TASKS] = "Buy flour."
        store.texts[LONE] = "Nothing here."
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `from a note it opens small, around that note, with its preview`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))

        val state = viewModel.state.value
        assertEquals(
            expected = Triple(GraphScope.THIS_NOTE, setOf(AREPAS, MENU), "Mix the flour. See Menu."),
            actual = Triple(state.scope, state.dots.map { it.path }.toSet(), state.preview?.snippet)
        )
    }

    @Test
    fun `depth two reaches the neighbors' neighbors`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))

        send(GraphIntent.OnDepthSelect(depth = 2))

        assertEquals(
            expected = setOf(AREPAS, MENU, TASKS),
            actual = viewModel.state.value.dots.map { it.path }.toSet()
        )
    }

    @Test
    fun `with no note it shows the vault, without orphans until asked`() = runGraphTest {
        send(GraphIntent.OnOpen(note = null))
        val hidden = viewModel.state.value.dots.map { it.path }.toSet()

        send(GraphIntent.OnOrphansToggle)

        assertEquals(
            expected = setOf(AREPAS, MENU, TASKS) to true,
            actual = hidden to (LONE in viewModel.state.value.dots.map { it.path })
        )
    }

    @Test
    fun `choosing a note around another moves it to the center`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))

        send(GraphIntent.OnNoteSelect(path = MENU))

        val center = viewModel.state.value.dots.single { it.depth == 0 }
        assertEquals(expected = MENU to 0f, actual = center.path to center.x)
    }

    @Test
    fun `an arrow key walks to a connected note, and Enter and Esc open a note`() = runGraphTest {
        val effects = mutableListOf<GraphEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        send(GraphIntent.OnOpen(note = AREPAS))
        val menu = viewModel.state.value.dots.single { it.path == MENU }
        val direction = if (menu.y < 0f) GraphDirection.UP else GraphDirection.DOWN

        send(GraphIntent.OnMove(direction = direction))
        send(GraphIntent.OnOpenSelected)
        send(GraphIntent.OnBack)

        assertEquals(
            expected = listOf<GraphEffect>(GraphEffect.OpenNote(path = MENU), GraphEffect.OpenNote(path = AREPAS)),
            actual = effects
        )
    }

    @Test
    fun `a folder of the legend is left out and remembered`() = runGraphTest {
        store.texts[TASKS] = "Back to [[Menu]]."
        send(GraphIntent.OnOpen(note = null))

        send(GraphIntent.OnFolderToggle(folder = "Guides"))

        assertEquals(
            expected = false to setOf("Guides"),
            actual = (TASKS in viewModel.state.value.dots.map { it.path }) to preferences.kept.excludedFolders
        )
    }

    @Test
    fun `a note with no links teaches how to link`() = runGraphTest {
        send(GraphIntent.OnOpen(note = LONE))

        assertEquals(expected = true, actual = viewModel.state.value.isEmpty)
    }

    @Test
    fun `search lights up the notes on the map and goes through them`() = runGraphTest {
        send(GraphIntent.OnOrphansToggle)
        send(GraphIntent.OnSearchOpen)
        send(GraphIntent.OnQueryChange(query = "a"))
        val first = viewModel.state.value.matches.first()

        send(GraphIntent.OnMatchStep(isForward = true))
        assertEquals(expected = first to first, actual = viewModel.state.value.selected to viewModel.state.value.revealed)

        send(GraphIntent.OnMatchStep(isForward = false))
        send(GraphIntent.OnSearchClose)
        val state = viewModel.state.value
        assertEquals(expected = Triple(false, "", null), actual = Triple(state.isSearching, state.query, state.revealed))
        assertEquals(expected = emptyList(), actual = state.matches)
    }

    @Test
    fun `around a note, search finds any note and moves there`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        send(GraphIntent.OnQueryChange(query = "tasks"))
        assertEquals(expected = listOf(TASKS), actual = viewModel.state.value.matches)

        send(GraphIntent.OnMatchStep(isForward = true))

        assertEquals(expected = TASKS, actual = viewModel.state.value.center)
    }

    @Test
    fun `a search with nothing found goes nowhere`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        send(GraphIntent.OnQueryChange(query = "zzz"))

        send(GraphIntent.OnMatchStep(isForward = true))

        assertEquals(expected = AREPAS, actual = viewModel.state.value.center)
    }

    @Test
    fun `Link joins the notes at once, and Undo takes it back`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        val text = store.texts[AREPAS]

        send(GraphIntent.OnLink(path = TASKS))
        assertEquals(expected = "$text\n\n[[Tasks]]\n", actual = store.texts[AREPAS])
        assertEquals(expected = TASKS, actual = viewModel.state.value.linked)
        assertEquals(expected = true, actual = viewModel.state.value.dots.any { it.path == TASKS })

        send(GraphIntent.OnLinkUndo)
        assertEquals(expected = text, actual = store.texts[AREPAS])
        assertEquals(expected = false, actual = viewModel.state.value.dots.any { it.path == TASKS })
    }

    @Test
    fun `Undo leaves a note edited since then alone, and says so`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        send(GraphIntent.OnLink(path = TASKS))
        store.texts[AREPAS] = "Edited meanwhile."

        send(GraphIntent.OnLinkUndo)

        assertEquals(expected = "Edited meanwhile.", actual = store.texts[AREPAS])
        assertEquals(expected = true, actual = viewModel.state.value.linkFailed)
    }

    @Test
    fun `the inspector suggests notes close in meaning that no link joins yet`() = runGraphTest(
        prepare = {
            store.texts[TASKS] = SAME_MEANING
            store.texts[LONE] = SAME_MEANING
            store.texts[MENU] = "$SAME_MEANING [[Tasks]]"
        }
    ) {
        indexed(paths = listOf(TASKS, LONE, MENU))
        send(GraphIntent.OnScopeSelect(scope = GraphScope.WHOLE_VAULT))

        send(GraphIntent.OnNoteSelect(path = TASKS))

        assertEquals(expected = listOf(LONE), actual = viewModel.state.value.preview?.suggested)
    }

    private suspend fun indexed(
        paths: List<RelativePath>
    ) {
        paths.forEach { path ->
            val chunks = chunkNote(note = path, text = store.texts.getValue(key = path))
            val embedded = embedChunks(embedder, title = path.nameWithoutExtension, chunks = chunks)
            val vectors = (embedded as Outcome.Success).value.map { (_, vector) -> vector }
            index.put(vaultPath = VAULT, note = path, chunks = chunks, vectors = vectors)
        }
    }

    private fun TestScope.send(
        intent: GraphIntent
    ) {
        viewModel.onIntent(intent = intent)
        advanceUntilIdle()
        runCurrent()
    }

    private fun runGraphTest(
        prepare: () -> Unit = {},
        body: suspend TestScope.() -> Unit
    ) = runTest(context = dispatcher) {
        try {
            prepare()
            send(GraphIntent.OnNotesChange(notes = listOf(AREPAS, MENU, TASKS, LONE).map { entry(path = it.value) }))
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    private companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val MENU = RelativePath(value = "Recipes/Menu.md")
        val TASKS = RelativePath(value = "Guides/Tasks.md")
        val LONE = RelativePath(value = "Lone.md")
        const val SAME_MEANING = "Corn flour and water and salt make a soft dough for the griddle every single morning."
    }
}
