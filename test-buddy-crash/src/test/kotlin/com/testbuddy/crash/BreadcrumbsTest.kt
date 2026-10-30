package com.testbuddy.crash

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BreadcrumbsTest {
    @AfterTest
    fun tearDown() {
        Breadcrumbs.clear()
    }

    @Test
    fun `steps are kept with their time, oldest first, the newest ones only`() {
        repeat(times = Breadcrumbs.MAX_STEPS + 5) { Breadcrumbs.record(step = "step $it") }

        val steps = Breadcrumbs.recent()
        assertEquals(Breadcrumbs.MAX_STEPS, steps.size)
        assertTrue(steps.first().endsWith(" step 5"))
        assertTrue(Regex("\\d\\d:\\d\\d:\\d\\d step ${Breadcrumbs.MAX_STEPS + 4}").matches(steps.last()))
    }

    @Test
    fun `the same step again is counted on its line`() {
        Breadcrumbs.record(step = "Editor: OnOpen")
        repeat(times = 3) { Breadcrumbs.record(step = "Editor: OnTextChange") }

        assertEquals(
            listOf("Editor: OnOpen", "Editor: OnTextChange (×3)"),
            Breadcrumbs.recent().map {
                it.substringAfter(' ')
            }
        )
    }
}
