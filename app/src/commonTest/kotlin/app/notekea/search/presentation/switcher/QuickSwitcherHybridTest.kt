package app.notekea.search.presentation.switcher

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.search.domain.PassageResult
import kotlin.test.Test
import kotlin.test.assertEquals

/** The palette takes the hybrid search's answer over the exact words it already shows. */
class QuickSwitcherHybridTest {
    private val exact = listOf(row(path = SONG), row(path = ARANCINI))

    @Test
    fun `passages come first, with where they are, then what only the exact words found, and the selection stays`() {
        val state = QuickSwitcherState(
            mode = QuickSwitcherMode.CONTENT,
            query = "corn",
            rows = exact,
            selectedIndex = 1
        )

        val answered = reduceHybridAnswer(
            state,
            exact,
            Outcome.Success(value = listOf(passage(path = AREPAS)))
        )

        assertEquals(
            expected = listOf(AREPAS, SONG, ARANCINI),
            actual = answered.rows.map { it.path }
        )

        assertEquals(
            expected = Triple(2, "Recipes › Dough", 4..8),
            actual = Triple(
                answered.selectedIndex,
                answered
                    .rows
                    .first()
                    .folder,
                answered
                    .rows
                    .first()
                    .passage
            )
        )
    }

    @Test
    fun `a note with every word always stays, and passages found by meaning give way`() {
        val tenExact = (0 until 10).map { row(path = RelativePath(value = "Exact $it.md")) }

        val state = QuickSwitcherState(
            mode = QuickSwitcherMode.CONTENT,
            query = "corn",
            rows = tenExact
        )

        val byMeaning = (0 until 10).map { passage(path = RelativePath(value = "Meaning $it.md")) }

        val answered = reduceHybridAnswer(state, tenExact, Outcome.Success(value = byMeaning))

        assertEquals(
            expected = tenExact.map { it.path },
            actual = answered.rows.map { it.path }
        )
    }

    @Test
    fun `a search that could not run keeps the exact words and says so`() {
        val state = QuickSwitcherState(
            mode = QuickSwitcherMode.CONTENT,
            query = "corn",
            rows = exact
        )

        val answered =
            reduceHybridAnswer(state, exact, Outcome.Failure(error = IndexRunError.Model))

        assertEquals(
            expected = Triple(listOf(SONG, ARANCINI), true, false),
            actual = Triple(
                answered.rows.map { it.path },
                answered.showsExactWords,
                answered.isSearching
            )
        )
    }

    @Test
    fun `only content search with something typed tells that the index is not ready`() {
        val notReady = QuickSwitcherState(
            mode = QuickSwitcherMode.CONTENT,
            query = "corn",
            isIndexReady = false
        )

        assertEquals(
            expected = listOf(true, false, false),
            actual = listOf(
                notReady.showsExactWords,
                notReady.copy(query = "").showsExactWords,
                notReady.copy(mode = QuickSwitcherMode.NOTES).showsExactWords
            )
        )
    }

    private fun row(path: RelativePath) = QuickSwitcherRow(
        path = path,
        name = path.nameWithoutExtension
    )

    private fun passage(path: RelativePath) = PassageResult(
        note = entry(path = path.value),
        snippet = "Mix the corn flour.",
        ranges = listOf(8..11),
        range = 4..8,
        heading = "Dough"
    )

    private companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val ARANCINI = RelativePath(value = "Arancini.md")
        val SONG = RelativePath(value = "Song.md")
    }
}
