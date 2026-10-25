package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FuzzyMatcherTest {
    private val matcher = FuzzyMatcher()

    @Test
    fun `the letters must appear in order, not necessarily together`() {
        assertEquals(
            expected = listOf(0..0, 8..10),
            actual = matcher.match(query = "pfag", text = PASTA)?.ranges
        )
        assertNull(actual = matcher.match(query = "gafp", text = PASTA))
    }

    @Test
    fun `letter case, accents and spaces in the search are ignored`() {
        assertEquals(
            expected = listOf(listOf(0..3), listOf(0..3), listOf(0..4, 8..10)),
            actual = listOf(
                matcher.match(query = "cafe", text = CAFE),
                matcher.match(query = "CAFÉ", text = "cafe"),
                matcher.match(query = "pasta fag", text = PASTA)
            ).map { it?.ranges }
        )
    }

    @Test
    fun `the best placement is highlighted, at the start of a word rather than the first letters found`() {
        assertEquals(
            expected = listOf(17..18),
            actual = matcher.match(query = "ar", text = CAFE)?.ranges
        )
    }

    @Test
    fun `a match at the start and with letters together ranks above one inside a longer text`() {
        val scores = listOf(AREPAS, CAFE, "Prepare dough").map { matcher.match(query = "are", text = it)?.score }

        assertTrue(actual = scores.zipWithNext().all { (better, worse) -> better!! > worse!! }, message = "$scores")
    }

    @Test
    fun `an empty search matches everything with no highlight`() {
        assertEquals(
            expected = FuzzyMatch.EVERYTHING,
            actual = matcher.match(query = "  ", text = AREPAS)
        )
    }

    companion object {
        private const val AREPAS = "Arepas"
        private const val CAFE = "Café con leche y arepas"
        private const val PASTA = "Pasta e fagioli"
    }
}
