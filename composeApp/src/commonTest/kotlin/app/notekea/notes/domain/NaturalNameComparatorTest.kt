package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.sign
import kotlin.random.Random

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

    @Test
    fun `the order is the one the earlier pattern-based comparison gave, for any names`() {
        val random = Random(seed = SEED)
        val names = List(size = NAME_COUNT) { randomName(random = random) }

        names.zipWithNext().forEach { (a, b) ->
            assertEquals(
                expected = referenceCompare(a = a, b = b).sign,
                actual = comparator.compare(a, b).sign,
                message = "\"$a\" against \"$b\""
            )
        }
    }

    private fun randomName(
        random: Random
    ): String = String(CharArray(size = random.nextInt(until = MAX_NAME_LENGTH)) { ALPHABET.random(random = random) })

    /** The comparison before 1.18, with its pattern, kept to prove the faster one orders names the same way. */
    private fun referenceCompare(
        a: String,
        b: String
    ): Int {
        val chunksA = CHUNK.findAll(input = a).map { it.value }.toList()
        val chunksB = CHUNK.findAll(input = b).map { it.value }.toList()
        chunksA.zip(chunksB).forEach { (chunkA, chunkB) ->
            val result = if (chunkA.first().isDigit() && chunkB.first().isDigit()) {
                compareValuesBy(chunkA.trimStart('0'), chunkB.trimStart('0'), { it.length }, { it })
            } else {
                chunkA.lowercase().compareTo(other = chunkB.lowercase())
            }
            if (result != 0) return result
        }
        return compareValues(chunksA.size, chunksB.size).takeIf { it != 0 } ?: a.compareTo(other = b)
    }

    companion object {
        private const val ALPHABET = "aAzZ0019 -_.éÉİıß٣"
        private const val MAX_NAME_LENGTH = 8
        private const val NAME_COUNT = 20_000
        private const val SEED = 118
        private val CHUNK = Regex(pattern = "\\d+|\\D+")
    }
}
