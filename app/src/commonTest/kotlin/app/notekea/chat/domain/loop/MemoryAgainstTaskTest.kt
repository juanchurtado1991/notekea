package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentFinalAnswer
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The memory and the task's own words (the loop's seventh run, 2026-11-17): "the household budget" against a memory
 * that names the other Budget 2026, and "my sister's phone" read from the note the memory names. Which note the task
 * means is the frame's (`docs/agent-frame.md`).
 */
class MemoryAgainstTaskTest {
    private val engine = FakeLlmEngine()

    private val home = RelativePath(value = "Home/Budget 2026.md")
    private val work = RelativePath(value = "Work/Budget 2026.md")

    private val budget = MemoryLine(
        section = MemorySection.PEOPLE,
        text = "\"the budget\" is [[Work/Budget 2026]]",
        origin = null
    )

    private fun given(meant: RelativePath) = TurnSetup.memoryOf(
        relevant = listOf(budget),
        frame = TaskFrame(
            targets = listOf(FrameTarget(note = meant)),
            count = FrameCount.ONE,
            candidates = listOf(home, work)
        )
    )

    // The frame, not a word list, says which note "the household budget" means: the memory's line about the other
    // one is not given, so it cannot pull the turn back to it. When the frame says the remembered one, it is given.
    @Test
    fun `a memory line naming a note the frame says the task does not mean is not given to the loop`() {
        assertEquals(
            expected = listOf(emptyList(), listOf(budget)),
            actual = listOf(given(meant = home), given(meant = work))
        )
    }

    @Test
    fun `the answer is written knowing what the memory says`() = runTest {
        engine.reply = { "Her phone is **8855-2310**." }

        AgentFinalAnswer(llmEngine = engine).written(
            task = "what is my sister's phone?",
            found = listOf("read \"Rosa Quesada\":\nphone: 8855-2310"),
            summary = "",
            memory = listOf("\"my sister\" is [[Rosa Quesada]]")
        )

        val asked = engine
            .generated
            .single()
            .joinToString(separator = "\n") { it.text }

        assertEquals(
            expected = true,
            actual = "- \"my sister\" is [[Rosa Quesada]]" in asked
        )
    }
}
