package app.notekea.ai.data

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The context the model's calls share (`docs/agent-loop.md`, step 2). Measured on 2026-11-14: calls that together
 * pass the context all fail in llama.cpp, the short ones with the long one, so the engine makes them wait.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TokenRoomTest {
    private val room = TokenRoom(
        total = 16_384,
        slots = 4
    )

    @Test
    fun `calls that together pass the context wait their turn, and none fails`() = runTest {
        val running = mutableListOf<String>()
        val longEnds = CompletableDeferred<Unit>()

        launch {
            room
                .withRoom(tokens = 11_000) {
                    running += "long"
                    longEnds.await()
                }
        }

        launch {
            room.withRoom(tokens = 2_300) { running += "short" }
        }

        launch {
            room.withRoom(tokens = 6_000) { running += "second long" }
        }

        runCurrent()
        val beside = running.toList()

        longEnds.complete(value = Unit)
        runCurrent()

        assertEquals(
            expected = listOf("long", "short") to listOf("long", "short", "second long"),
            actual = beside to running.toList()
        )
    }

    @Test
    fun `no more calls run side by side than there are slots, however short`() = runTest {
        var running = 0
        var most = 0
        val gate = CompletableDeferred<Unit>()

        repeat(times = 6) {
            launch {
                room
                    .withRoom(tokens = 100) {
                        running++
                        most = maxOf(most, running)
                        gate.await()
                        running--
                    }
            }
        }

        runCurrent()
        val held = most
        gate.complete(value = Unit)
        runCurrent()

        assertEquals(
            expected = 4 to 0,
            actual = held to running
        )
    }

    @Test
    fun `a call that fails gives its room back, and one longer than the whole context is told beforehand`() =
        runTest {
            runCatching {
                room.withRoom(tokens = 16_000) { error("the model failed") }
            }

            var ran = false
            room.withRoom(tokens = 16_000) { ran = true }

            assertEquals(
                expected = listOf(true, true, false),
                actual = listOf(ran, room.fits(tokens = 16_384), room.fits(tokens = 16_385))
            )
        }
}
