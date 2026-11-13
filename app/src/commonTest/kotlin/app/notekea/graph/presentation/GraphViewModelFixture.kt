package app.notekea.graph.presentation

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.FuzzyMatcher
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.index.ChunkNoteUseCase
import app.notekea.core.notes.domain.index.FindRelatedNotesUseCase
import app.notekea.core.notes.domain.index.embedChunks
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.graph.domain.BuildNoteGraphUseCase
import app.notekea.graph.domain.GraphPreferences
import app.notekea.graph.domain.GraphPreferencesStore
import app.notekea.graph.domain.LinkNotesUseCase
import app.notekea.graph.presentation.model.GraphIntent
import app.notekea.graph.presentation.viewmodel.GraphViewModel
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

/** A vault of a few linked notes under the real graph and layout, for the tests of the graph tab. */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class GraphViewModelFixture {
    protected val dispatcher = StandardTestDispatcher()
    protected val store = FakeNoteContentStore()
    protected val embedder = FakeTextEmbedder()
    protected val index = FakeVectorIndex()
    protected val chunkNote =
        ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder()))
    protected val preferences = KeptPreferences()
    protected val viewModel by lazy {
        val folder = TextFolder()
        val dispatchers = FakeAppDispatchers(dispatcher = dispatcher)
        val finder = JetBrainsMarkdownLinkFinder()

        val cache = NoteTextCache(
            contentStore = store,
            textFolder = folder
        )

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

    protected suspend fun indexed(paths: List<RelativePath>) {
        paths.forEach { path ->
            val chunks = chunkNote(
                note = path,
                text = store.texts.getValue(key = path)
            )

            val embedded = embedChunks(
                embedder,
                title = path.nameWithoutExtension,
                chunks = chunks
            )

            val vectors = (embedded as Outcome.Success).value.map { (_, vector) -> vector }

            index.put(
                vaultPath = VAULT,
                note = path,
                chunks = chunks,
                vectors = vectors
            )
        }
    }

    protected fun TestScope.send(intent: GraphIntent) {
        viewModel.onIntent(intent = intent)
        advanceUntilIdle()
        runCurrent()
    }

    protected fun runGraphTest(
        prepare: () -> Unit = {},
        body: suspend TestScope.() -> Unit
    ) = runTest(context = dispatcher) {
        try {
            prepare()

            send(
                GraphIntent.OnNotesChange(
                    notes = listOf(AREPAS, MENU, TASKS, LONE).map { entry(path = it.value) }
                )
            )

            send(GraphIntent.OnShownChange(isShown = true))
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    protected class KeptPreferences : GraphPreferencesStore {
        var kept = GraphPreferences()

        override suspend fun read(vaultPath: String): GraphPreferences = kept

        override suspend fun write(
            vaultPath: String,
            preferences: GraphPreferences
        ) {
            kept = preferences
        }
    }

    protected companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val MENU = RelativePath(value = "Recipes/Menu.md")
        val TASKS = RelativePath(value = "Guides/Tasks.md")
        val LONE = RelativePath(value = "Lone.md")
        const val SAME_MEANING =
            "Corn flour and water and salt make a soft dough for the griddle every single morning."
    }
}
