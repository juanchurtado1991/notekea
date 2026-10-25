package app.notekea.core.presentation.platform

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowCloseGuardTest {
    private val guard = WindowCloseGuard()

    @Test
    fun `closing runs every registered handler, the most recent first, and none once removed`() = runTest {
        val ran = mutableListOf<String>()
        guard.register { ran += "tree" }
        val removeEditor = guard.register { ran += "editor" }

        guard.beforeClose()
        removeEditor()
        guard.beforeClose()

        assertEquals(
            expected = listOf("editor", "tree", "tree"),
            actual = ran
        )
    }

    @Test
    fun `a stuck handler is given up after its timeout, so the window still closes`() = runTest {
        val ran = mutableListOf<String>()
        guard.register { ran += "after" }
        guard.register { awaitCancellation() }

        guard.beforeClose()

        assertEquals(
            expected = listOf("after"),
            actual = ran
        )
    }
}
