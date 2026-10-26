package app.notekea.notes.domain

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.entry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SearchNoteContentUseCaseTest {
    private val store = FakeNoteContentStore().apply { suspendsLikeDisk = true }

    @Test
    fun `every word must appear in the body, ignoring case and accents, most matches first`() = runTest {
        val notes = notesWith(
            AREPAS to "# Arepas\n\nMix the corn flour with water.\nCorn arepas go well with café.\n",
            PASTA to "Beans and corn.\n",
            WELCOME to "Hello\n"
        )
        val search = search()

        assertEquals(
            expected = listOf(AREPAS to 2, PASTA to 1),
            actual = search(VAULT, notes, "corn").map { it.note.file.path.value to it.matchCount }
        )
        assertEquals(
            expected = listOf(AREPAS),
            actual = search(VAULT, notes, "CORN cafe").map { it.note.file.path.value }
        )
    }

    @Test
    fun `the snippet is the first matching line with the matches highlighted`() = runTest {
        val notes = notesWith(AREPAS to "# Arepas\n\n  Mix the corn flour, more corn.\n")

        assertEquals(
            expected = "Mix the corn flour, more corn." to listOf(8..11, 25..28),
            actual = search()(VAULT, notes, "corn").single().let { it.snippet to it.ranges }
        )
    }

    @Test
    fun `a long line is shortened around the first match`() = runTest {
        val line = "a".repeat(n = 100) + " corn " + "b".repeat(n = 200)
        val result = search()(VAULT, notesWith(AREPAS to line), "corn").single()
        val snippet = result.snippet.orEmpty()

        assertEquals(
            expected = Triple(true, true, "corn"),
            actual = Triple(
                snippet.startsWith(prefix = "…"),
                snippet.endsWith(suffix = "…"),
                snippet.substring(range = result.ranges.single())
            )
        )
    }

    @Test
    fun `the frontmatter is not searched as text`() = runTest {
        val notes = notesWith(AREPAS to "---\ntags: [recipe]\n---\nCorn.\n")

        assertEquals(
            expected = emptyList(),
            actual = search()(VAULT, notes, "tags")
        )
    }

    @Test
    fun `a tag matches the frontmatter or the text, nested tags too, but not code or longer tags`() = runTest {
        val tagged = entry(path = AREPAS, metadata = NoteFixtures.tags("Recipe"))
        store.texts[tagged.file.path] = "---\ntags: [Recipe]\n---\nCorn.\n"
        val notes = listOf(tagged) + notesWith(
            PASTA to "Dinner #recipe/venezuelan tonight\n",
            WELCOME to "```\n#recipe\n```\n",
            IDEAS to "#recipes are many\n"
        )

        assertEquals(
            // One match each, so by name.
            expected = listOf(AREPAS to emptyList(), PASTA to listOf(7..24)),
            actual = search()(VAULT, notes, "#recipe").map { it.note.file.path.value to it.ranges }
        )
    }

    @Test
    fun `a second search reads only the notes that changed and forgets removed ones`() = runTest {
        val notes = notesWith(AREPAS to "Corn\n", PASTA to "Beans\n")
        val search = search()
        search(VAULT, notes, "corn")
        store.reads.clear()

        val changed = notes[1].copy(file = notes[1].file.copy(modifiedAt = notes[1].file.modifiedAt?.plus(1.seconds)))
        store.texts[changed.file.path] = "Beans and corn\n"
        val results = search(VAULT, listOf(changed), "corn")

        assertEquals(
            expected = listOf(RelativePath(value = PASTA)) to listOf(PASTA),
            actual = store.reads.toList() to results.map { it.note.file.path.value }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a cancelled search stops reading`() = runTest {
        val notes = notesWith(*(1..200).map { "Note $it.md" to "Corn\n" }.toTypedArray())
        val search = search()
        val job = launch { search(VAULT, notes, "corn") }
        // Cancelled in the middle of the second batch of reads, as the next keystroke would.
        store.onRead = { if (store.reads.size == CANCEL_AFTER_READS) job.cancel() }

        runCurrent()

        assertTrue(actual = store.reads.size <= READS_IN_TWO_BATCHES, message = "${store.reads.size} reads")
    }

    @Test
    fun `an empty search finds nothing and reads nothing`() = runTest {
        val notes = notesWith(AREPAS to "Corn\n")

        assertEquals(
            expected = emptyList<ContentSearchResult>() to 0,
            actual = search()(VAULT, notes, "  ") to store.reads.size
        )
    }

    private fun TestScope.search(): SearchNoteContentUseCase {
        val folder = TextFolder()
        return SearchNoteContentUseCase(
            noteTextCache = NoteTextCache(contentStore = store, textFolder = folder),
            textFolder = folder,
            naturalNameComparator = NaturalNameComparator(),
            markdownLineStripper = MarkdownLineStripper(),
            dispatchers = FakeAppDispatchers(dispatcher = StandardTestDispatcher(scheduler = testScheduler))
        )
    }

    private fun notesWith(
        vararg texts: Pair<String, String>
    ): List<NoteEntry> = texts.map { (path, text) ->
        store.texts[RelativePath(value = path)] = text
        entry(path = path)
    }

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val CANCEL_AFTER_READS = 40
        private const val READS_IN_TWO_BATCHES = 64
        private const val IDEAS = "Ideas.md"
        private const val PASTA = "Recipes/Pasta e fagioli.md"
        private const val WELCOME = "Welcome.md"
    }
}
