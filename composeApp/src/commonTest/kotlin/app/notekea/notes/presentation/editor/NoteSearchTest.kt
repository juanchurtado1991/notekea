package app.notekea.notes.presentation.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class NoteSearchTest {
    private val search = NoteSearch()

    @Test
    fun `every match is found, ignoring letter case`() {
        assertEquals(
            expected = listOf(0..5, 19..24),
            actual = search.matches(text = "Arepas with cheese arepas", query = "AREPAS")
        )
    }

    @Test
    fun `matches never overlap, and an empty query finds nothing`() {
        assertEquals(
            expected = listOf(listOf(0..1, 2..3), emptyList()),
            actual = listOf(search.matches(text = "aaaaa", query = "aa"), search.matches(text = "aaaaa", query = ""))
        )
    }

    @Test
    fun `accented and non latin text is found as typed`() {
        assertEquals(
            expected = listOf(10..13),
            actual = search.matches(text = "Leche con café", query = "CAFÉ")
        )
    }
}
