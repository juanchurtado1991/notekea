package app.notekea.chat.domain.loop
import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.agent.Remembering
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolCallJson
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
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The loop's checks before a turn ends (`docs/agent-loop.md`, "Why the loop fails", items 3 and 4): a task that can
 * mean several notes is asked back, and a change to several notes is listed back once to drop what does not fit.
 */
class TurnVerifierTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    private val lease = RelativePath(value = "Home/Lease.md")
    private val instructions = RelativePath(value = "Notekea/Notekea.md")
    private val office = RelativePath(value = "Work/Lease.md")
    private val notes = listOf(lease, instructions).map { entry(path = it.value) }

    private val runTurn = RunTurnUseCase(
        llmEngine = engine,
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
        vaultGuide = VaultGuide(noteTextCache = cache),
        proposal = ProposalCheck(
            checkPlan = CheckPlanUseCase(),
            composeNotes = ComposeNotesUseCase(
                llmEngine = engine,
                noteTextCache = cache,
                planReader = GhostActionPlanReader()
            )
        ),
        clock = Clock.System,
        frames = FrameSource { _, _, _, _, _ -> frames.lastOrNull() }
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
        skill: String = "none"
    ) {
        val left = ArrayDeque(elements = calls.toList())
        val route = """{"work":"$work","skill":"$skill"}"""

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> route
                asked.startsWith(prefix = CALLS) -> left.removeFirst()
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

    @Test
    fun `a change to several notes is listed back once with what each holds, and the ones dropped are left out`() =
        runTest {
            val books =
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

            script(
                """{"tool":"add_tag","notes":["Dune","Sapiens"],"tag":"later"}""",
                """{"tool":"propose","summary":"$SUMMARY"}""",
                """{"tool":"propose","summary":"$SUMMARY","drop":["Sapiens"]}""",
                work = "organize"
            )

            val steps = run(
                task = "tag the books I rated 4 with later",
                notes = notes + books
            )

            assertEquals(
                expected = listOf(true, listOf("Dune")),
                actual = listOf(
                    "- Dune: rating: 4\n- Sapiens: rating: 3\n" in callPrompts()[2],
                    (steps.last() as AgentStep.Proposed)
                        .steps
                        .single()
                        .notes
                        .map { it.nameWithoutExtension }
                )
            )
        }

    private companion object {
        const val SUMMARY = "Tags the books."
        const val CALLS = "You work on a task in the person's notes"
    }
}
