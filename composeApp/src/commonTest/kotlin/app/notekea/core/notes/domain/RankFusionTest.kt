package app.notekea.core.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** Two rankings merged by their ranks (`ROADMAP.md` 2.3): high in both beats first in one. */
class RankFusionTest {
    @Test
    fun `an item high in both rankings comes before one first in only one, and ties keep first appearance`() {
        val merged = RankFusion.merge(rankings = listOf(listOf("a", "b", "c"), listOf("b", "d", "a")))

        assertEquals(expected = listOf("b", "a", "d", "c"), actual = merged.map { it.first })
    }

    @Test
    fun `one empty ranking leaves the other in its order`() {
        assertEquals(
            expected = listOf("x", "y"),
            actual = RankFusion.merge(rankings = listOf(listOf("x", "y"), emptyList())).map { it.first }
        )
    }
}
