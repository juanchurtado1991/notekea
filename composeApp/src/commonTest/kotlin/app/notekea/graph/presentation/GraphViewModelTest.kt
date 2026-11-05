package app.notekea.graph.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.FindRelatedNotesUseCase
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.graph.domain.BuildNoteGraphUseCase
import app.notekea.graph.domain.GraphPreferences
import app.notekea.graph.domain.GraphPreferencesStore
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
                chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder())),
                textEmbedder = FakeTextEmbedder(),
                vectorIndex = FakeVectorIndex(),
                noteContentStore = store,
                linkFinder = finder
            ),
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

    private fun TestScope.send(
        intent: GraphIntent
    ) {
        viewModel.onIntent(intent = intent)
        advanceUntilIdle()
        runCurrent()
    }

    private fun runGraphTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = dispatcher) {
        try {
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
    }
}
