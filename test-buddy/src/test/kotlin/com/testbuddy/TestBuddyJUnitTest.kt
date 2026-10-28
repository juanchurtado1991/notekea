package com.testbuddy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestBuddyJUnitTest {
    private val suite = sampleSuite {
        automated(id = "1.1-01", title = "Shows the button") { expectText(text = "Save later") }
        automated(id = "1.1-02", title = "Shows a ghost") { expectText(text = "Ghost") }
    }

    @Test
    fun `each automated case becomes a test named after its ID, which fails as the case does`() {
        val tests = object : TestBuddyJUnit(suite = suite) {}.automatedCases()
        assertEquals(listOf("1.1-01 Shows the button", "1.1-02 Shows a ghost"), tests.map { it.displayName })
        tests[0].executable.execute()
        val failure = assertFailsWith<AssertionError> { tests[1].executable.execute() }
        assertTrue(failure.message.orEmpty().startsWith("expected to see \"Ghost\", it did not appear (screenshot: "))
    }
}
