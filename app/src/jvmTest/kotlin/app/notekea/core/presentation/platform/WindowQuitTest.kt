package app.notekea.core.presentation.platform

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.awt.desktop.QuitResponse
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals

/** Whatever ends Notekea, what is being typed is saved first, once (audit 4). */
class WindowQuitTest {
    private val closeGuard = WindowCloseGuard()
    private val steps = CopyOnWriteArrayList<String>()
    private val quit = WindowQuit(closeGuard = closeGuard, scope = CoroutineScope(context = Dispatchers.Default))

    private fun saving() {
        closeGuard.register {
            delay(timeMillis = SAVE_MILLIS)
            steps += SAVED
        }
    }

    @Test
    fun `the system's quit goes ahead only once the guard is done`() = runBlocking {
        saving()
        val answered = CompletableDeferred<Unit>()
        val response = object : QuitResponse {
            override fun performQuit() {
                steps += QUIT
                answered.complete(value = Unit)
            }

            override fun cancelQuit() {
                steps += CANCELLED
            }
        }

        SystemQuit(quit = quit).onQuit(response = response)
        withTimeout(timeMillis = WAIT_MILLIS) { answered.await() }

        assertEquals(
            expected = listOf(SAVED, QUIT),
            actual = steps.toList()
        )
    }

    @Test
    fun `a second way of quitting waits for the same run of the guard`() = runBlocking {
        saving()
        val first = CompletableDeferred<Unit>()
        val second = CompletableDeferred<Unit>()

        quit.request {
            steps += CLOSED
            first.complete(value = Unit)
        }
        quit.request {
            steps += QUIT
            second.complete(value = Unit)
        }
        withTimeout(timeMillis = WAIT_MILLIS) { awaitAll(first, second) }

        assertEquals(
            expected = SAVED to setOf(CLOSED, QUIT),
            actual = steps.first() to steps.drop(n = 1).toSet()
        )
    }

    @Test
    fun `a shutdown waits for the guard, and is at once after any other quit`() {
        saving()

        quit.finishBeforeShutdown()
        val saved = steps.toList()
        quit.finishBeforeShutdown()

        assertEquals(
            expected = listOf(listOf(SAVED), listOf(SAVED)),
            actual = listOf(saved, steps.toList())
        )
    }

    private companion object {
        const val CANCELLED = "cancelled"
        const val CLOSED = "closed"
        const val QUIT = "quit"
        const val SAVED = "saved"
        const val SAVE_MILLIS = 50L
        const val WAIT_MILLIS = 5_000L
    }
}
