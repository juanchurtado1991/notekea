package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class NaturalNameComparatorTest {
    private val comparator = NaturalNameComparator()

    @Test
    fun `numbers compare by value and letter case is ignored`() {
        val names = listOf("Note 10", "note 2", "Note 1", "2026-10-10", "2026-10-9", "apple", "Banana", "Note 02b")

        assertEquals(
            expected = listOf("2026-10-9", "2026-10-10", "apple", "Banana", "Note 1", "note 2", "Note 02b", "Note 10"),
            actual = names.sortedWith(comparator)
        )
    }

    @Test
    fun `huge numbers and names equal except for case still have a stable order`() {
        val names = listOf("v99999999999999999999999", "v100000000000000000000000", "Arepas", "arepas", "Arepa")

        assertEquals(
            expected = listOf("Arepa", "Arepas", "arepas", "v99999999999999999999999", "v100000000000000000000000"),
            actual = names.sortedWith(comparator)
        )
    }
}
