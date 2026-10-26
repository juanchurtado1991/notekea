package app.notekea.notes.presentation.switcher

import androidx.lifecycle.viewModelScope
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FuzzyMatcher
import app.notekea.notes.domain.MarkdownLineStripper
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.entry
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.NoteTextCache
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SearchNoteContentUseCase
import app.notekea.notes.domain.SearchNotesUseCase
import app.notekea.notes.domain.TextFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The palette over the real search use cases and a content store that suspends like the disk. */
@OptIn(ExperimentalCoroutinesApi::class)
class QuickSwitcherViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore().apply { suspendsLikeDisk = true }
    private val notes = listOf(
        note(path = AREPAS, text = "Mix the corn flour.\n", modified = 3, aliases = listOf("Corn cakes")),
        note(path = CAFE, text = "Coffee with milk. #drinks\n", modified = 2),
        note(path = PASTA, text = "Beans and corn.\n", modified = 1),
        note(path = WELCOME, text = "Start with [[Arepas]].\n", modified = 0)
    )
    private val viewModel by lazy {
        val folder = TextFolder()
        val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        val naturalNames = NaturalNameComparator()
        QuickSwitcherViewModel(
            vaultPath = VAULT,
            searchNotes = SearchNotesUseCase(
                fuzzyMatcher = FuzzyMatcher(textFolder = folder),
                naturalNameComparator = naturalNames
            ),
            searchNoteContent = SearchNoteContentUseCase(
                noteTextCache = NoteTextCache(contentStore = store, textFolder = folder),
                textFolder = folder,
                naturalNameComparator = naturalNames,
                markdownLineStripper = MarkdownLineStripper(),
                dispatchers = dispatchers
            ),
            dispatchers = dispatchers
        ).apply { onIntent(intent = QuickSwitcherIntent.OnNotesChange(notes = notes)) }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the palette opens on the most recent notes, and typing finds names and aliases at once`() = runSwitcherTest {
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES))
        val recent = names()
        send(QuickSwitcherIntent.OnQueryChange(query = "cakes"))

        assertEquals(
            expected = listOf("Arepas", "Café con leche", "Pasta e fagioli", "Welcome"),
            actual = recent
        )
        assertEquals(
            expected = listOf(
                QuickSwitcherRow(
                    path = RelativePath(value = AREPAS),
                    name = "Arepas",
                    detailRanges = listOf(5..9),
                    folder = "Recipes",
                    detail = "Corn cakes"
                )
            ),
            actual = viewModel.state.value.rows
        )
    }

    @Test
    fun `content search waits for a pause in typing, then shows the matching lines`() = runSwitcherTest {
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT))
        send(QuickSwitcherIntent.OnQueryChange(query = "co"))
        advanceTimeBy(delayTimeMillis = TYPING_PAUSE_MILLIS)
        send(QuickSwitcherIntent.OnQueryChange(query = "corn"))
        val whileTyping = viewModel.state.value.let { it.isSearching to it.rows }

        advanceTimeBy(delayTimeMillis = AFTER_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(
            expected = true to emptyList(),
            actual = whileTyping
        )
        assertEquals(
            expected = listOf(
                "Arepas" to "Mix the corn flour.",
                "Pasta e fagioli" to "Beans and corn."
            ) to false,
            actual = viewModel.state.value.let { state -> state.rows.map { it.name to it.detail } to state.isSearching }
        )
    }

    @Test
    fun `a slow search never replaces the results of a newer one`() = runSwitcherTest {
        // The content search for "corn" is still reading notes when Tab switches to names, which answer at once.
        store.onRead = {
            store.onRead = {}
            viewModel.onIntent(intent = QuickSwitcherIntent.OnModeSwitch)
        }
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT, query = "corn"))
        advanceTimeBy(delayTimeMillis = AFTER_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(
            expected = QuickSwitcherMode.NOTES to listOf("Arepas"),
            actual = viewModel.state.value.mode to names()
        )
    }

    @Test
    fun `arrows wrap around, and enter opens the selected note and closes the palette`() = runSwitcherTest {
        val effects = collectEffects()
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES))
        send(QuickSwitcherIntent.OnSelectPrevious)
        val wrapped = viewModel.state.value.selectedIndex
        send(QuickSwitcherIntent.OnSelectNext)
        send(QuickSwitcherIntent.OnSelectNext)
        send(QuickSwitcherIntent.OnConfirm)

        assertEquals(
            expected = 3 to listOf(QuickSwitcherEffect.OpenNote(path = RelativePath(value = CAFE))),
            actual = wrapped to effects
        )
        assertEquals(
            expected = QuickSwitcherState(),
            actual = viewModel.state.value
        )
    }

    @Test
    fun `closing without choosing says so, so the keyboard can go back to the note`() = runSwitcherTest {
        val effects = collectEffects()
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES))
        send(QuickSwitcherIntent.OnClose)

        assertEquals(
            expected = listOf<QuickSwitcherEffect>(QuickSwitcherEffect.Dismissed),
            actual = effects
        )
    }

    @Test
    fun `tab switches between names and content, keeping the text`() = runSwitcherTest {
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES, query = "corn"))
        val byName = names()
        send(QuickSwitcherIntent.OnModeSwitch)

        assertEquals(
            expected = listOf("Arepas"),
            actual = byName
        )
        assertEquals(
            expected = Triple(QuickSwitcherMode.CONTENT, "corn", listOf("Arepas", "Pasta e fagioli")),
            actual = viewModel.state.value.let { Triple(it.mode, it.query, names()) }
        )
    }

    @Test
    fun `a tag opens content search with the tag typed, and clicking a result opens it`() = runSwitcherTest {
        val effects = collectEffects()
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT, query = "#drinks"))
        val found = names()
        send(QuickSwitcherIntent.OnRowClick(path = RelativePath(value = CAFE)))

        assertEquals(
            expected = listOf("Café con leche"),
            actual = found
        )
        assertEquals(
            expected = listOf(QuickSwitcherEffect.OpenNote(path = RelativePath(value = CAFE))),
            actual = effects
        )
    }

    @Test
    fun `a note chosen to link is linked instead of opened, and linking stays on names`() = runSwitcherTest {
        val effects = collectEffects()
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES, query = "cafe", purpose = LINK))
        send(QuickSwitcherIntent.OnModeSwitch)
        val mode = viewModel.state.value.mode
        send(QuickSwitcherIntent.OnConfirm)
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES))

        assertEquals(
            expected = Triple(
                QuickSwitcherMode.NOTES,
                listOf(QuickSwitcherEffect.LinkNote(path = RelativePath(value = CAFE))),
                QuickSwitcherPurpose.OPEN
            ),
            actual = Triple(mode, effects.toList(), viewModel.state.value.purpose)
        )
    }

    @Test
    fun `notes that change while the palette is open update the results`() = runSwitcherTest {
        send(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES, query = "ideas"))
        val before = names()
        send(QuickSwitcherIntent.OnNotesChange(notes = notes + note(path = "Ideas.md", text = "", modified = 4)))

        assertEquals(
            expected = emptyList<String>() to listOf("Ideas"),
            actual = before to names()
        )
    }

    private fun note(
        path: String,
        text: String,
        modified: Long,
        aliases: List<String> = emptyList()
    ): NoteEntry {
        store.texts[RelativePath(value = path)] = text
        return entry(path = path, modifiedOffsetMillis = modified, metadata = NoteMetadata.NONE.copy(aliases = aliases))
    }

    private fun TestScope.send(
        intent: QuickSwitcherIntent
    ) {
        viewModel.onIntent(intent = intent)
        runCurrent()
    }

    private fun TestScope.collectEffects(): List<QuickSwitcherEffect> {
        val effects = mutableListOf<QuickSwitcherEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        runCurrent()
        return effects
    }

    private fun names(): List<String> = viewModel.state.value.rows.map { it.name }

    /** Runs [body], then stops the ViewModel's scope so no search outlives the test. */
    private fun runSwitcherTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    companion object {
        private val LINK = QuickSwitcherPurpose.LINK
        private const val AFTER_DEBOUNCE_MILLIS = 151L
        private const val AREPAS = "Recipes/Arepas.md"
        private const val CAFE = "Recipes/Café con leche.md"
        private const val PASTA = "Recipes/Pasta e fagioli.md"
        private const val TYPING_PAUSE_MILLIS = 100L
        private const val WELCOME = "Welcome.md"
    }
}
