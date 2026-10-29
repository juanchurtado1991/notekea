package com.testbuddy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class OnlyCasesTest {
    private val suite = sampleSuite {
        listOf("1.6-01", "1.6-12", "1.16-01", "1.17-02").forEach { automated(id = it, title = it) {} }
        manual(id = "1.6-05", reason = ManualReason.EYES) {
            title(text = "T")
            steps(text = "S")
            expected(text = "E")
        }
    }

    @Test
    fun `a feature keeps its own cases, not those of a feature whose number starts the same`() {
        val only = suite.only(prefixes = listOf("1.6"))
        assertEquals(listOf("1.6-01", "1.6-12"), only.automated.map { it.id })
        assertEquals(listOf("1.6-05"), only.manual.map { it.id })
    }

    @Test
    fun `a case ID keeps that case, and no prefix keeps the whole suite`() {
        assertEquals(listOf("1.17-02"), suite.only(prefixes = listOf("1.17-02")).automated.map { it.id })
        assertSame(suite, suite.only(prefixes = emptyList()))
    }
}
