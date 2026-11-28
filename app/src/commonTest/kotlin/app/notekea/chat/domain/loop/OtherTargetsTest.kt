package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.tasks.NoteTask
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The last look before a change to one thing is proposed (`docs/agent-frame.md`): the line and the note to change are
 * known by then, so what else the request could be about is found beside them, marked, and asked of the person.
 */
class OtherTargetsTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)

    private val plants = RelativePath(value = "Home/Plants.md")
    private val garden = RelativePath(value = "Garden/Plants.md")

    private val settled = TaskFrame(
        targets = listOf(FrameTarget(note = plants)),
        count = FrameCount.ONE,
        candidates = listOf(plants),
        words = "the watering"
    )

    private fun script(
        change: String,
        marks: String
    ) {
        val calls = ArrayDeque(elements = listOf(change, PROPOSE))

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"organize","skill":"none"}"""
                asked.startsWith(prefix = MARK) -> marks
                asked.startsWith(prefix = CALLS) && "(Not proposed" in messages.last().text -> ASK
                asked.startsWith(prefix = CALLS) -> calls.removeFirstOrNull() ?: PROPOSE
                else -> "Done."
            }
        }
    }

    private fun entryOf(path: RelativePath) = entry(
        path = path.value,
        metadata = NoteMetadata.NONE.copy(
            tasks = listOf("Water the fern", "Water the cactus").map {
                NoteTask(
                    stateIndex = 0,
                    endIndex = 0,
                    text = it,
                    isDone = false
                )
            }
        )
    )

    private suspend fun endOf(
        frame: TaskFrame = settled,
        task: String = "mark the watering of my plants as done",
        notes: List<RelativePath> = listOf(plants),
        openNote: RelativePath? = null
    ): AgentStep = loopTurn(
        engine = engine,
        cache = cache,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ORGANIZE to listOf(
                    ChangeTools.completeTask,
                    ChangeTools.addTask,
                    ChangeTools.addTag,
                    AskUserTool(),
                    ProposeTool()
                )
            )
        ),
        frames = FrameSource { _, _, _, _, _, _ -> frame }
    )(
        task = task,
        vaultPath = VAULT,
        notes = notes.map { entryOf(path = it) },
        context = ActionContext(
            today = "2026-11-25",
            notes = notes.map { it.bare() }
        ),
        openNote = openNote
    ).toList().last()

    private fun heldWith(): String = engine
        .generated
        .map { it.last().text }
        .lastOrNull {
            "(Not proposed" in it
        }
        .orEmpty()
        .substringAfter(delimiter = "does not say which: ")
        .substringBefore(delimiter = ". Ask the person")

    @Test
    fun `a task completed beside another the request could be about is held, and the person is asked`() =
        runTest {
            store.texts[plants] = WATERING

            script(
                change = COMPLETE,
                marks = """{"E1":"yes","E2":"yes"}"""
            )

            assertEquals(
                expected = listOf(true, "- [ ] Water the fern; - [ ] Water the cactus"),
                actual = listOf(endOf() is AgentStep.Asked, heldWith())
            )
        }

    @Test
    fun `a change to the only entry the request could be about is proposed`() = runTest {
        store.texts[plants] = WATERING

        script(
            change = COMPLETE,
            marks = """{"E1":"yes","E2":"no"}"""
        )

        assertEquals(
            expected = listOf(true, ""),
            actual = listOf(endOf() is AgentStep.Proposed, heldWith())
        )
    }

    @Test
    fun `a change to a note is held when another shares its name and the request could be about both`() =
        runTest {
            store.texts[plants] = "Ferns."
            store.texts[garden] = "Roses."

            script(
                change = """{"tool":"add_tag","notes":["Home/Plants"],"tag":"green"}""",
                marks = """{"N1":"yes","N2":"yes"}"""
            )

            val end = endOf(
                task = "tag my plants green",
                frame = settled.copy(words = "my plants"),
                notes = listOf(plants, garden)
            )

            assertEquals(
                expected = listOf(true, setOf("Home/Plants", "Garden/Plants")),
                actual = listOf(end is AgentStep.Asked, heldWith().split("; ").toSet())
            )
        }

    @Test
    fun `the open note, one the memory names and a change to several are the person's own words for it`() =
        runTest {
            store.texts[plants] = WATERING

            val ends = listOf(
                settled to plants,
                settled.copy(reason = FrameReason.MEMORY) to null,
                settled.copy(count = FrameCount.EVERY) to null
            ).map { (frame, openNote) ->
                script(
                    change = COMPLETE,
                    marks = """{"E1":"yes","E2":"yes"}"""
                )

                endOf(
                    frame = frame,
                    openNote = openNote
                ) is AgentStep.Proposed
            }

            assertEquals(
                expected = listOf(true, true, true),
                actual = ends
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val MARK = "A request made to a notes app is about one"
        const val PROPOSE = """{"tool":"propose","summary":"Done."}"""
        const val ASK =
            """{"tool":"ask_user","question":"Which one?","options":["One","Another"]}"""

        const val COMPLETE =
            """{"tool":"complete_task","note":"Home/Plants","text":"Water the fern"}"""

        const val WATERING = "## Watering\n- [ ] Water the fern\n- [ ] Water the cactus"
    }
}
