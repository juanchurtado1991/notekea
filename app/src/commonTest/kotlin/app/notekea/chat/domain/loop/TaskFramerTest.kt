package app.notekea.chat.domain.loop

import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The frame's model call (`docs/agent-frame.md`): it may only name what it was shown, by number, under a grammar;
 * an entry picked names its note; a reply cut short or with no count is no frame, and with nothing to show no call
 * is made.
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

    private suspend fun frameOf(reply: String): TaskFrame? {
        engine.reply = { reply }

        return framer.frameOf(
            task = "move the appointment to 11:00",
            candidates = listOf(home, dentist),
            memory = emptyList()
        )
    }

    @Test
    fun `the notes and entries picked by number become the frame, an entry naming its note`() =
        runTest {
            val frame =
                frameOf(reply = """{"notes":[],"entries":["E1","E2"],"count":"one","words":"the appointment"}""")

            assertEquals(
                expected = listOf(listOf(dentist.path), true, dentist.entries, listOf(home.path)),
                actual = listOf(frame?.notes, frame?.isOpen, frame?.options, frame?.others)
            )
        }

    @Test
    fun `the grammar names only what was shown, and asks for no entries when none were shown`() =
        runTest {
            frameOf(reply = """{"notes":["N1"],"entries":[],"count":"one","words":""}""")
            engine.reply = { """{"notes":["N1"],"count":"one","words":""}""" }

            framer.frameOf(
                task = "my budget",
                candidates = listOf(home),
                memory = emptyList()
            )

            val schemas = engine.options.map { it.jsonSchema.orEmpty() }

            assertEquals(
                expected = listOf(true, true, false, false),
                actual = listOf(
                    "\"N2\"" in schemas.first() && "\"E2\"" in schemas.first(),
                    "\"N3\"" !in schemas.first() && "\"E3\"" !in schemas.first(),
                    "entries" in schemas.last(),
                    "\"N2\"" in schemas.last()
                )
            )
        }

    @Test
    fun `a reply cut short or with no count is no frame, and nothing shown makes no call`() =
        runTest {
            val cut = frameOf(reply = """{"notes":["N1"],"count":"o""")
            val noCount = frameOf(reply = """{"notes":["N1"],"words":""}""")
            val calls = engine.generated.size

            val nothing = framer.frameOf(
                task = "hello",
                candidates = emptyList(),
                memory = emptyList()
            )

            assertEquals(
                expected = listOf(null, null, null, calls),
                actual = listOf(cut, noCount, nothing, engine.generated.size)
            )
        }
}
