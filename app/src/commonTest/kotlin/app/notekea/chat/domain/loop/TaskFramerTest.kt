package app.notekea.chat.domain.loop

import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The frame's two questions (`docs/agent-frame.md`): how many things the task means, from its words alone; then
 * which shown note and entry, named only by number under a grammar, or every one its words fit when it is unclear.
 */
class TaskFramerTest {
    private val engine = FakeLlmEngine()
    private val framer = TaskFramer(llmEngine = engine)

    private val home = FrameCandidates.Candidate(
        path = RelativePath(value = "Home/Budget.md"),
        gist = "The household's money.",
        entries = emptyList()
    )

    private val dentist = FrameCandidates.Candidate(
        path = RelativePath(value = "Health/Dentist.md"),
        gist = "Dr. Paz.",
        entries = listOf("- 12/01 10:00 cleaning", "- 12/09 16:30 filling")
    )

    // The model counts with [count]; then picks with [pick].
    private suspend fun frameOf(
        count: String,
        pick: String,
        candidates: List<FrameCandidates.Candidate> = listOf(home, dentist)
    ): TaskFrame? {
        engine.reply = { messages ->
            if (messages.first().text == FramePrompts.COUNT) count else pick
        }

        return framer.frameOf(
            task = "move the appointment to 11:00",
            candidates = candidates,
            memory = emptyList()
        )
    }

    @Test
    fun `one thing meant and two entries fitting it is open between the entries`() = runTest {
        val frame = frameOf(
            count = """{"count":"one","words":"the appointment"}""",
            pick = """{"note":"N2","fit":[],"entry":"unclear","fit_entries":["E1","E2"]}"""
        )

        assertEquals(
            expected = listOf(true, dentist.entries, "the appointment"),
            actual = listOf(frame?.isOpen, frame?.options, frame?.words)
        )
    }

    @Test
    fun `an unclear note is every note its words fit, and a clear one is settled`() = runTest {
        val unclear = frameOf(
            count = """{"count":"one","words":"it"}""",
            pick = """{"note":"unclear","fit":["N1","N2"],"entry":"none","fit_entries":[]}"""
        )

        val clear = frameOf(
            count = """{"count":"one","words":"it"}""",
            pick = """{"note":"N1","fit":[],"entry":"none","fit_entries":[]}"""
        )

        assertEquals(
            expected = listOf(listOf(home.path, dentist.path), home.path),
            actual = listOf(unclear?.openNotes, clear?.meant)
        )
    }

    @Test
    fun `the count is asked without notes, and the pick names only what was shown`() = runTest {
        frameOf(
            count = """{"count":"one","words":""}""",
            pick = """{"note":"N1","fit":[]}""",
            candidates = listOf(home)
        )

        val counted = engine
            .generated
            .first()
            .last()
            .text

        val schemas = engine.options.map { it.jsonSchema.orEmpty() }

        assertEquals(
            expected = listOf(false, true, false, false),
            actual = listOf(
                "Home/Budget" in counted,
                "\"N1\"" in schemas.last(),
                "\"N2\"" in schemas.last(),
                "entry" in schemas.last()
            )
        )
    }

    @Test
    fun `every and none ask nothing more, a reply cut short is no frame, and nothing shown makes no call`() =
        runTest {
            val every = frameOf(
                count = """{"count":"every","words":"my recipes"}""",
                pick = ""
            )

            val calls = engine.generated.size

            val cut = frameOf(
                count = """{"count":"o""",
                pick = ""
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
