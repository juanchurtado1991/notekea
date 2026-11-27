package app.notekea.chat.domain.loop
import app.notekea.chat.data.GhostTextNotesReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.agent.Remembering
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.reading.OutlineTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The loop's checks before a turn ends (`docs/agent-loop.md`, "Why the loop fails", items 3 and 4): a task that can
 * mean several notes is asked back, and each note of a change to several is marked before the card.
 */
class TurnVerifierTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)

    private val lease = RelativePath(value = "Home/Lease.md")
    private val instructions = RelativePath(value = "Notekea/Notekea.md")
    private val office = RelativePath(value = "Work/Lease.md")
    private val notes = listOf(lease, instructions).map { entry(path = it.value) }

    private val runTurn = loopTurn(
        engine = engine,
        cache = cache,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ASK to listOf(
                    OutlineTool(noteTextCache = cache),
                    ReadTool(noteTextCache = cache),
                    CalculateTool(),
                    AskUserTool(),
                    AnswerTool()
                ),
                WorkKind.ORGANIZE to listOf(
                    ChangeTools.addTag,
                    ChangeTools.createNote,
                    AskUserTool(),
                    ProposeTool()
                )
            )
        ),
        frames = FrameSource { _, _, _, _, _, _ -> frames.lastOrNull() }
    )

    // The frame a test gives its turn, as the frame's model would say it; none when a test gives none.
    private val frames = mutableListOf<TaskFrame>()

    private val lauras = listOf("People/Laura Castro.md", "People/Laura Méndez.md").map {
        RelativePath(value = it)
    }

    // "the phone of Laura": both Lauras fit, and the task means one.
    private val bothLauras = TaskFrame(
        targets = lauras.map { FrameTarget(note = it) },
        count = FrameCount.ONE,
        candidates = lauras,
        words = "Laura"
    )

    private fun script(
        vararg calls: String,
        work: String = "ask",
        skill: String = "none",
        marks: String = ""
    ) {
        val left = ArrayDeque(elements = calls.toList())
        val route = """{"work":"$work","skill":"$skill"}"""

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> route
                asked.startsWith(prefix = CALLS) -> left.removeFirst()
                asked.startsWith(prefix = REVIEW) -> marks
                else -> "The rent is **11,400 euros** a year."
            }
        }
    }

    private suspend fun run(
        task: String = "how much is the rent a year?",
        notes: List<NoteEntry> = this.notes
    ): List<AgentStep> = runTurn(
        task = task,
        vaultPath = VAULT,
        notes = notes,
        context = ActionContext(
            today = "2026-11-14",
            notes = listOf("Home/Lease")
        ),
        openNote = null
    ).toList()

    private fun callPrompts(): List<String> = engine
        .generated
        .filter {
            it
                .first()
                .text
                .startsWith(prefix = CALLS)
        }
        .map { it.last().text }

    @Test
    fun `it calls a tool, sees what it gave back, and ends on an answer written from what it found`() =
        runTest {
            store.texts[lease] = "# Lease\n\n## Rent\nThe rent is 950 euros a month."
            store.texts[instructions] = "Answer in one sentence."

            script(
                """{"tool":"read","note":"Lease","section":"Rent"}""",
                """{"tool":"calculate","expression":"950 * 12"}""",
                """{"tool":"answer","text":"11,400 euros a year."}"""
            )

            val steps = run()

            assertEquals(
                expected = listOf(
                    listOf("read Home/Lease › Rent", "calculate 950 * 12"),
                    AgentStep.Finished(summary = "The rent is **11,400 euros** a year."),
                    true,
                    true,
                    true
                ),
                actual = listOf(
                    steps.filterIsInstance<AgentStep.Moving>().map { "${it.move} ${it.target}" },
                    steps.last(),
                    "950 euros a month" in callPrompts()[1],
                    "calculate: 950 * 12 = 11400" in callPrompts()[2],
                    "Answer in one sentence." in engine
                        .generated[1]
                        .first()
                        .text
                )
            )
        }

    @Test
    fun `an unknown tool and a call cut short are told to the model, which goes on`() = runTest {
        store.texts[lease] = "The rent is 950 euros a month."

        script(
            """{"tool":"search_web","query":"rent"}""",
            """{"tool":"read","note":"Lea""",
            """{"tool":"read","note":"Lease"}""",
            """{"tool":"answer","text":"950 euros a month."}"""
        )

        val steps = run()

        assertEquals(
            expected = listOf(true, true, true, 4),
            actual = listOf(
                steps.last() is AgentStep.Finished,
                "No tool \"search_web\"" in callPrompts()[1],
                LoopPrompt.UNREAD in callPrompts()[2],
                callPrompts().size
            )
        )
    }

    @Test
    fun `an answer about one of two people the task does not tell apart is held, and only a question may follow`() =
        runTest {
            val people = listOf("People/Laura Castro.md", "People/Laura Méndez.md").map {
                entry(path = it)
            }

            frames += bothLauras

            script(
                """{"tool":"answer","text":"Laura's phone is 8701-9032."}""",
                """{"tool":"ask_user","question":"Which Laura?","options":["Laura Castro","Laura Méndez"]}"""
            )

            val steps = run(
                task = "what is the phone of Laura?",
                notes = notes + people
            )

            val schema = engine
                .options
                .mapNotNull { it.jsonSchema }
                .last()

            assertEquals(
                expected = listOf(true, false, true),
                actual = listOf(
                    "the task means one of 2 notes and does not say which: People/Laura Castro; People/Laura Méndez" in
                        callPrompts()[1],
                    "\"answer\"" in schema,
                    steps.last() is AgentStep.Asked
                )
            )
        }

    // Code review, 2026-11-15: held on the last step, the turn ran out and answered anyway, never asking.
    @Test
    fun `an answer held on the last step is followed by one call more, the question`() = runTest {
        val people = listOf("People/Laura Castro.md", "People/Laura Méndez.md").map {
            entry(path = it)
        }

        frames += bothLauras

        val sums = (1 until RunTurnUseCase.MAX_STEPS).map {
            """{"tool":"calculate","expression":"$it + 1"}"""
        }

        script(
            *sums.toTypedArray(),
            """{"tool":"answer","text":"Laura's phone is 8701-9032."}""",
            """{"tool":"ask_user","question":"Which Laura?","options":["Laura Castro","Laura Méndez"]}"""
        )

        val steps = run(
            task = "what is the phone of Laura?",
            notes = notes + people
        )

        assertEquals(
            expected = AgentStep.Asked(
                question = "Which Laura?",
                options = listOf("Laura Castro", "Laura Méndez"),
                // docs/agent-memory.md: a question about notes the task left open can be remembered once answered.
                remembering = Remembering(
                    said = "Laura",
                    notes = listOf("People/Laura Castro.md", "People/Laura Méndez.md").map {
                        RelativePath(value = it)
                    }
                )
            ),
            actual = steps.last()
        )
    }

    private val books =
        listOf("4" to "Books/Dune.md", "3" to "Books/Sapiens.md").map { (rating, path) ->
            entry(
                path = path,
                metadata = NoteMetadata.NONE.copy(
                    properties = listOf(
                        NoteProperty(
                            key = "rating",
                            value = PropertyValue.Number(text = rating)
                        )
                    )
                )
            )
        }

    // The card of a change to both books, each marked as [marks] says.
    private suspend fun cardWith(marks: String): AgentStep.Proposed {
        script(
            """{"tool":"add_tag","notes":["Dune","Sapiens"],"tag":"later"}""",
            """{"tool":"propose","summary":"$SUMMARY"}""",
            work = "organize",
            marks = marks
        )

        val steps = run(
            task = "tag the books I rated 4 with later",
            notes = notes + books
        )

        return steps.last() as AgentStep.Proposed
    }

    // The notes left on that card.
    private suspend fun taggedWith(marks: String): List<String> = cardWith(marks = marks)
        .steps
        .single()
        .notes
        .map { it.nameWithoutExtension }

    // Dev, 2026-11-24: asked to name the notes to drop, the Gemma E4B named those it meant to keep.
    @Test
    fun `each note of a change to several is marked by what it holds, and the ones marked no are left out`() =
        runTest {
            val tagged = taggedWith(marks = """{"N1":"yes","N2":"no"}""")

            val shown = engine
                .generated
                .single {
                    it
                        .first()
                        .text
                        .startsWith(prefix = REVIEW)
                }
                .last()
                .text

            assertEquals(
                expected = listOf(listOf("Dune"), true),
                actual = listOf(tagged, "N1 Dune: rating: 4\nN2 Sapiens: rating: 3" in shown)
            )
        }

    @Test
    fun `marks that say nothing, every note no or a reply cut short, leave the change as it was`() =
        runTest {
            assertEquals(
                expected = List(size = 2) { listOf("Dune", "Sapiens") },
                actual = listOf(
                    taggedWith(marks = """{"N1":"no","N2":"no"}"""),
                    taggedWith(marks = """{"N1":"n""")
                )
            )
        }

    // Dev, 2026-11-25: the card's summary, written before the review, named the notes it had taken out.
    @Test
    fun `a card the review took a note out of drops the summary that names it, and any other keeps its own`() =
        runTest {
            assertEquals(
                expected = listOf("", SUMMARY),
                actual = listOf(
                    cardWith(marks = """{"N1":"yes","N2":"no"}""").summary,
                    cardWith(marks = """{"N1":"yes","N2":"yes"}""").summary
                )
            )
        }

    private companion object {
        const val SUMMARY = "Tags the books."
        const val CALLS = "You work on a task in the person's notes"
        const val REVIEW = "A person's request changes notes"
    }
}
