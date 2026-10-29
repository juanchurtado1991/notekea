package com.testbuddy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShardCasesTest {
    private val suite = sampleSuite {
        (1..7).forEach { automated(id = "1.1-0$it", title = "Case $it") {} }
        manual(id = "1.2-01", reason = ManualReason.EYES) {
            title(text = "T")
            steps(text = "S")
            expected(text = "E")
        }
    }

    @Test
    fun `the parts take turns, cover every case once and keep the manual cases in the first`() {
        val parts = (0 until 3).map { suite.shard(index = it, count = 3) }
        assertEquals(
            listOf(listOf("1.1-01", "1.1-04", "1.1-07"), listOf("1.1-02", "1.1-05"), listOf("1.1-03", "1.1-06")),
            parts.map { part -> part.automated.map { it.id } }
        )
        assertEquals(listOf(1, 0, 0), parts.map { it.manual.size })
    }

    @Test
    fun `a part that does not exist is refused`() {
        assertFailsWith<IllegalArgumentException> { suite.shard(index = 3, count = 3) }
        assertFailsWith<IllegalArgumentException> { suite.shard(index = 0, count = 0) }
    }
}
