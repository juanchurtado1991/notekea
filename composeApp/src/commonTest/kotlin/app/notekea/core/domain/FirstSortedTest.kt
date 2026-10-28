package app.notekea.core.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class FirstSortedTest {
    @Test
    fun `the first elements are those a full sort gives, ties in their original order`() {
        val random = Random(seed = SEED)
        val byScore = compareBy<Pair<Int, Int>> { it.first }
        repeat(times = ROUNDS) {
            // Few scores for many elements, so ties are everywhere; the second value tells equal ones apart.
            val elements = List(size = random.nextInt(until = MAX_SIZE)) { random.nextInt(until = SCORES) to it }
            val n = random.nextInt(until = MAX_SIZE)

            assertEquals(
                expected = elements.sortedWith(byScore).take(n = n),
                actual = elements.firstSorted(n = n, comparator = byScore)
            )
        }
    }

    companion object {
        private const val MAX_SIZE = 60
        private const val ROUNDS = 500
        private const val SCORES = 5
        private const val SEED = 118
    }
}
