package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.entry
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchNotesUseCaseTest {
    private val search = SearchNotesUseCase(
        fuzzyMatcher = FuzzyMatcher(),
        naturalNameComparator = NaturalNameComparator()
    )
    private val notes = listOf(
        entry(path = AREPAS, modifiedOffsetMillis = 3),
        entry(
            path = CAFE,
            modifiedOffsetMillis = 1,
            metadata = NoteMetadata.NONE.copy(aliases = listOf("Morning coffee"))
        ),
        entry(path = PREPARE, modifiedOffsetMillis = 2),
        entry(path = WELCOME, metadata = NoteMetadata.NONE.copy(title = "Start here"))
    )

    @Test
    fun `notes match by name, best first, down to letters scattered in an alias`() {
        // "Start here" (Welcome's title) holds a, r and e in order, far apart, so it comes last.
        assertEquals(
            expected = listOf(AREPAS, CAFE, PREPARE, WELCOME),
            actual = paths(results = search(notes = notes, query = "are"))
        )
    }

    @Test
    fun `an alias or the frontmatter title matches too, and says so with its own highlight`() {
        assertEquals(
            expected = listOf(
                NoteSearchResult(
                    note = notes[1],
                    field = NoteMatchField.ALIAS,
                    text = "Morning coffee",
                    ranges = listOf(0..6)
                ),
                NoteSearchResult(
                    note = notes[3],
                    field = NoteMatchField.ALIAS,
                    text = "Start here",
                    ranges = listOf(0..4)
                )
            ),
            actual = listOf("morning", "start").map { search(notes = notes, query = it).single() }
        )
    }

    @Test
    fun `the name wins over an alias that matches as well`() {
        val note = entry(path = AREPAS, metadata = NoteMetadata.NONE.copy(aliases = listOf("Arepas reina")))

        assertEquals(
            expected = NoteMatchField.NAME,
            actual = search(notes = listOf(note), query = "arepas").single().field
        )
    }

    @Test
    fun `a search with a slash also matches the path`() {
        assertEquals(
            expected = listOf(listOf(NoteMatchField.PATH), emptyList()),
            actual = listOf("rec/are", "recare").map { query ->
                search(notes = notes, query = query).filter { it.note == notes[0] }.map { it.field }
            }
        )
    }

    @Test
    fun `an empty search lists the most recently modified notes, up to the limit`() {
        val results = search(notes = notes, query = " ", limit = 3)

        assertEquals(
            expected = listOf(AREPAS, PREPARE, CAFE),
            actual = paths(results = results)
        )
        assertEquals(
            expected = setOf(NoteMatchField.RECENT),
            actual = results.map { it.field }.toSet()
        )
    }

    private fun paths(
        results: List<NoteSearchResult>
    ): List<String> = results.map { it.note.file.path.value }

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val CAFE = "Recipes/Café con leche y arepas.md"
        private const val PREPARE = "Prepare dough.md"
        private const val WELCOME = "Welcome.md"
    }
}
