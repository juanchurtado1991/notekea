package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FakeFileWatcher
import app.notekea.notes.domain.FakeNoteMetadataCache
import app.notekea.notes.domain.FuzzyMatcher
import app.notekea.notes.domain.MarkdownLineStripper
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteMatchField
import app.notekea.notes.domain.NoteTextCache
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.SearchNoteContentUseCase
import app.notekea.notes.domain.SearchNotesUseCase
import app.notekea.notes.domain.TextFolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The quick switcher cases of `MVP_SMOKE_TEST.md` (1.6) that can run headless, on the `Personal Notes` smoke vault
 * with the production scanner, frontmatter reader and content store: every result the smoke guide promises is checked
 * here against the real data, so the guide never states a value the vault does not hold.
 */
class SmokeQuickSwitcherTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val folder = TextFolder()
    private val naturalNames = NaturalNameComparator()
    private val searchNotes = SearchNotesUseCase(
        fuzzyMatcher = FuzzyMatcher(textFolder = folder),
        naturalNameComparator = naturalNames
    )
    private val searchContent = SearchNoteContentUseCase(
        noteTextCache = NoteTextCache(
            contentStore = OkioNoteContentStore(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers),
            textFolder = folder
        ),
        textFolder = folder,
        naturalNameComparator = naturalNames,
        markdownLineStripper = MarkdownLineStripper(),
        dispatchers = dispatchers
    )

    @Test
    fun `1_6-01 typing are puts Arepas first, its first letters highlighted`() = runTest(context = testDispatcher) {
        val first = searchNotes(notes = loadNotes(), query = "are").first()

        assertEquals(
            expected = Triple(AREPAS, NoteMatchField.NAME, listOf(0..2)),
            actual = Triple(first.note.file.path.value, first.field, first.ranges)
        )
    }

    @Test
    fun `1_6-02 an alias finds its note and accents are ignored`() = runTest(context = testDispatcher) {
        val notes = loadNotes()
        val alias = searchNotes(notes = notes, query = "corn cakes").first()

        assertEquals(
            expected = Triple(AREPAS, "Corn cakes", NoteMatchField.ALIAS),
            actual = Triple(alias.note.file.path.value, alias.text, alias.field)
        )
        assertEquals(
            expected = CAFE,
            actual = searchNotes(notes = notes, query = "cafe").first().note.file.path.value
        )
    }

    @Test
    fun `1_6-03 a search with a slash matches the path`() = runTest(context = testDispatcher) {
        val first = searchNotes(notes = loadNotes(), query = "gard/tom").first()

        assertEquals(
            expected = Triple(TOMATO_PLAN, NoteMatchField.PATH, "Projects/Garden/Tomato plan"),
            actual = Triple(first.note.file.path.value, first.field, first.text)
        )
    }

    @Test
    fun `1_6-04 content search finds the word inside longer words, most matches first, lines as plain text`() = runTest(
        context = testDispatcher
    ) {
        val results = searchContent(VAULT, loadNotes(), "tomato")

        assertEquals(
            expected = listOf(
                Triple(
                    JOURNAL_20,
                    2,
                    "Planted the last tomato seedlings; notes in Tomato plan. Cooked " +
                        "Arepas for dinner, the cheese ones were best."
                ),
                Triple(PASTA, 1, "Add tomatoes, cooked borlotti beans and stock; simmer 20 minutes."),
                Triple(TOMATO_PLAN, 1, "Tomato plan")
            ),
            actual = results.map { Triple(it.note.file.path.value, it.matchCount, it.snippet) }
        )
    }

    @Test
    fun `1_6-05 a tag finds frontmatter tags and nested tags, not other tags`() = runTest(context = testDispatcher) {
        val results = searchContent(VAULT, loadNotes(), "#recipe")

        assertEquals(
            expected = listOf(AREPAS to null, CAFE to "#recipe/drinks", PASTA to "#recipe"),
            actual = results.map { it.note.file.path.value to it.snippet }
        )
    }

    @Test
    fun `1_6-07 the travel tag finds the note tagged in its frontmatter and the journal entry`() = runTest(
        context = testDispatcher
    ) {
        val results = searchContent(VAULT, loadNotes(), "#travel")

        assertEquals(
            expected = listOf(JOURNAL_21 to "#journal #travel", LISBON to null),
            actual = results.map { it.note.file.path.value to it.snippet }
        )
    }

    private suspend fun loadNotes(): Collection<NoteEntry> {
        val observeVaultTree = ObserveVaultTreeUseCase(
            noteScanner = OkioNoteScanner(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers),
            noteMetadataReader = OkioNoteMetadataReader(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = dispatchers,
                parser = GhostFrontmatterParser()
            ),
            fileWatcher = FakeFileWatcher(),
            noteMetadataCache = FakeNoteMetadataCache()
        )
        return (observeVaultTree(vaultPath = VAULT).first() as Outcome.Success).value.notes.values
    }

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val CAFE = "Recipes/Café con leche.md"
        private const val JOURNAL_20 = "Journal/2026-10-20.md"
        private const val JOURNAL_21 = "Journal/2026-10-21.md"
        private const val LISBON = "Travel/Lisbon trip.md"
        private const val PASTA = "Recipes/Pasta e fagioli.md"
        private const val TOMATO_PLAN = "Projects/Garden/Tomato plan.md"
        private val VAULT = ("../smoke-data/vaults".toPath() / "Personal Notes").toString()
    }
}
