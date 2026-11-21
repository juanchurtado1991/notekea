package app.notekea.chat.data

import app.notekea.chat.domain.context.StepChunk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The turn's steps in SQLite's full-text table (`docs/agent-step-memory.md`), ranked by BM25. */
class SqliteStepMemoryTest {
    private val memory = SqliteStepMemory()

    @Test
    fun `the parts that match the need come back, the best first, a word matched by its start`() =
        runTest {
            memory.keep(
                space = "turn",
                entry = 0,
                chunks = listOf("fetch athenify", "Técnicas para memorizar: repetición espaciada.", "Precio y planes.")
            )

            memory.keep(
                space = "turn",
                entry = 1,
                chunks = listOf("read Applied statistics", "The z-score and the p value.")
            )

            assertEquals(
                expected = listOf(
                    StepChunk(
                        entry = 0,
                        chunk = 1
                    )
                ),
                actual = memory.ranked(
                    space = "turn",
                    need = "investiga las mejores técnicas para memorizar",
                    entries = setOf(0, 1),
                    most = 5
                )
            )
        }

    @Test
    fun `only the asked results and the turn's own space are ranked, and a forgotten turn holds nothing`() =
        runTest {
            memory.keep(
                space = "a",
                entry = 0,
                chunks = listOf("compost", "Greens and browns for compost.")
            )

            memory.keep(
                space = "b",
                entry = 0,
                chunks = listOf("compost", "Compost in a bin.")
            )

            val asked = memory.ranked(
                space = "a",
                need = "compost",
                entries = setOf(1),
                most = 5
            )

            memory.forget(space = "a")

            val forgotten = memory.ranked(
                space = "a",
                need = "compost",
                entries = setOf(0),
                most = 5
            )

            val other = memory.ranked(
                space = "b",
                need = "compost",
                entries = setOf(0),
                most = 5
            )

            assertEquals(
                expected = listOf(
                    emptyList(),
                    emptyList(),
                    listOf(StepChunk(0, 0), StepChunk(0, 1))
                ),
                actual = listOf(asked, forgotten, other.sortedBy { it.chunk })
            )
        }
}
