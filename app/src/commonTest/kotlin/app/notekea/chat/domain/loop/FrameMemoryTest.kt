package app.notekea.chat.domain.loop

import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The note of a set the person's memory says the task's words mean, settled by the code (`docs/agent-memory.md`):
 * the loop's run of 2026-11-17 asked "which Laura?" with "Laura" is [[Laura Méndez]] in the memory.
 */
class FrameMemoryTest {
    private fun candidate(path: String) = FrameCandidates.Candidate(
        path = RelativePath(value = path),
        gist = "",
        entries = emptyList()
    )

    private fun line(text: String) = MemoryLine(
        section = MemorySection.PEOPLE,
        text = text,
        origin = null
    )

    private val castro = candidate(path = "People/Laura Castro.md")
    private val mendez = candidate(path = "People/Laura Méndez.md")
    private val home = candidate(path = "Home/Budget 2026.md")
    private val work = candidate(path = "Work/Budget 2026.md")

    private val memory = listOf(
        line(text = "\"Laura\" is [[Laura Méndez]]"),
        line(text = "\"José\" is [[Laura Castro]]"),
        line(text = "\"the budget\" and \"el presupuesto\" are [[Work/Budget 2026]]")
    )

    @Test
    fun `a line naming one note of the set settles it, by title or path, and none leaves it open`() {
        assertEquals(
            expected = listOf(mendez, work, work, null, castro, null),
            actual = listOf(
                FrameMemory.settledOf(
                    task = "what is Laura's phone number?",
                    group = listOf(castro, mendez),
                    memory = memory
                ),
                FrameMemory.settledOf(
                    task = "what is the total of the budget?",
                    group = listOf(home, work),
                    memory = memory
                ),
                FrameMemory.settledOf(
                    task = "¿cuánto suma el presupuesto 2026?",
                    group = listOf(home, work),
                    memory = memory
                ),
                FrameMemory.settledOf(
                    task = "what is the total of the budgets?",
                    group = listOf(home, work),
                    memory = memory
                ),
                FrameMemory.settledOf(
                    task = "José's email",
                    group = listOf(castro, mendez),
                    memory = memory
                ),
                FrameMemory.settledOf(
                    task = "Joséfina's email",
                    group = listOf(castro, mendez),
                    memory = memory
                )
            )
        )
    }
}
