package app.notekea.chat.domain.loop

import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The frame's small questions (`docs/agent-frame.md`): the code finds the namesakes a task's words fit, the model
 * only says whether the task says which; for a change, which entry of the note; each named by number under a grammar.
 */
class TaskFramerTest {
    private val engine = FakeLlmEngine()
    private val framer = TaskFramer(llmEngine = engine)

    private val home = candidate(path = "Home/Budget.md")
    private val work = candidate(path = "Work/Budget.md")

    private val dentist = candidate(
        path = "Health/Dentist.md",
        entries = listOf("- 12/01 10:00 cleaning", "- 12/09 16:30 filling")
    )

    private fun candidate(
        path: String,
        entries: List<String> = emptyList()
    ) = FrameCandidates.Candidate(
        path = RelativePath(value = path),
        gist = "",
        entries = entries
    )

    // Each question's reply, by the prompt that asks it.
    private suspend fun frameOf(
        task: String,
        candidates: List<FrameCandidates.Candidate>,
        replies: Map<String, String>,
        isChange: Boolean = false
    ): TaskFrame? {
        engine.reply = { messages -> replies[messages.first().text].orEmpty() }

        return framer.frameOf(
            task = task,
            candidates = candidates,
            memory = emptyList(),
            isChange = isChange
        )
    }

    private val one = """{"count":"one","words":"the budget"}"""

    @Test
    fun `namesakes the task does not say which of are open, and one it says is settled`() =
        runTest {
            val open = frameOf(
                task = "what is the total of the budget?",
                candidates = listOf(home, work),
                replies = mapOf(FramePrompts.COUNT to one, FramePrompts.SAID to """{"pick":"not said"}""")
            )

            val said = frameOf(
                task = "what is the total of the work budget?",
                candidates = listOf(home, work),
                replies = mapOf(FramePrompts.COUNT to one, FramePrompts.SAID to """{"pick":"N2"}""")
            )

            assertEquals(
                expected = listOf(listOf(home.path, work.path), work.path),
                actual = listOf(open?.openNotes, said?.meant)
            )
        }

    @Test
    fun `a change to one entry the task does not say which of is open between the entries`() =
        runTest {
            val frame = frameOf(
                task = "move the appointment to 11:00",
                candidates = listOf(dentist),
                replies = mapOf(
                    FramePrompts.COUNT to """{"count":"one","words":"the appointment"}""",
                    FramePrompts.PICK to """{"pick":"N1"}""",
                    FramePrompts.ENTRY to """{"pick":"not said"}"""
                ),
                isChange = true
            )

            assertEquals(
                expected = listOf(true, dentist.entries),
                actual = listOf(frame?.isOpen, frame?.options)
            )
        }

    @Test
    fun `the count is asked without notes, and a question about a note asks no entry`() = runTest {
        frameOf(
            task = "when is the appointment?",
            candidates = listOf(dentist),
            replies = mapOf(FramePrompts.COUNT to one, FramePrompts.PICK to """{"pick":"N1"}""")
        )

        val asked = engine.generated.map { it.first().text }

        assertEquals(
            expected = listOf(false, listOf(FramePrompts.COUNT, FramePrompts.PICK)),
            actual = listOf(
                "Health/Dentist" in engine
                    .generated
                    .first()
                    .last()
                    .text,
                asked
            )
        )
    }

    @Test
    fun `every asks nothing more, a reply cut short is no frame, and nothing shown makes no call`() =
        runTest {
            val every = frameOf(
                task = "tag my recipes",
                candidates = listOf(home),
                replies = mapOf(FramePrompts.COUNT to """{"count":"every","words":"my recipes"}""")
            )

            val calls = engine.generated.size

            val cut = frameOf(
                task = "tag it",
                candidates = listOf(home),
                replies = mapOf(FramePrompts.COUNT to """{"count":"o""")
            )

            val before = engine.generated.size

            val nothing = framer.frameOf(
                task = "hello",
                candidates = emptyList(),
                memory = emptyList()
            )

            assertEquals(
                expected = listOf(true, 1, null, null, before),
                actual = listOf(every?.isSeveral, calls, cut, nothing, engine.generated.size)
            )
        }
}
