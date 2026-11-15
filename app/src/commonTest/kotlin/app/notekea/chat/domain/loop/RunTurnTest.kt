package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
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
 * The one loop with a scripted model (`docs/agent-loop.md`, step 5), replies that are wrong on purpose among them: an
 * unknown tool, a call cut short, a call made again. The loop feeds the error back and recovers.
 */
class RunTurnTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    private val lease = RelativePath(value = "Home/Lease.md")
    private val instructions = RelativePath(value = "Notekea.md")
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
        clock = Clock.System
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
    fun `a call made again is told, and made again twice the next call can only end the turn`() =
        runTest {
            store.texts[lease] = "The rent is 950 euros a month."
            val read = """{"tool":"read","note":"Lease"}"""
            script(read, read, read, """{"tool":"answer","text":"950 euros a month."}""")
            val steps = run()

            val last = engine
                .options
                .filter {
                    it.jsonSchema != null
                }
                .last()
                .jsonSchema
                .orEmpty()

            assertEquals(
                expected = listOf(true, true, false, true),
                actual = listOf(
                    steps.last() is AgentStep.Finished,
                    "again: its result is above" in callPrompts()[2],
                    "\"read\"" in last,
                    "\"answer\"" in last && "\"ask_user\"" in last
                )
            )
        }

    @Test
    fun `asked back in place of a guess, the turn ends on the question and its answers`() =
        runTest {
            script("""{"tool":"ask_user","question":"Which lease?","options":["The flat's","The office's"]}""")

            assertEquals(
                expected = listOf<AgentStep>(
                    AgentStep.Asked(
                        question = "Which lease?",
                        options = listOf("The flat's", "The office's")
                    )
                ),
                actual = run(task = "summarize the lease")
            )
        }

    @Test
    fun `routed to a kind of work it gets that work's tools and the skill, and its changes end on the card`() =
        runTest {
            script(
                """{"tool":"add_tag","notes":["Tacos"],"tag":"home"}""",
                """{"tool":"add_tag","notes":["Lease"],"tag":"home"}""",
                """{"tool":"propose","summary":"$SUMMARY"}""",
                work = "organize",
                skill = "meeting notes"
            )

            val steps = run(task = "tag the lease as home")

            val system = engine
                .generated[1]
                .first()
                .text

            val proposed = steps.last() as AgentStep.Proposed
            val tagged = listOf(ActionKind.ADD_TAG)

            assertEquals(
                expected = listOf(true, false, true, true, tagged, listOf(lease), SUMMARY),
                actual = listOf(
                    "- add_tag (notes, tag):" in system,
                    "- calculate" in system,
                    "For this kind of work (meeting notes):" in system,
                    "add_tag: not added, UnknownNote(name=Tacos)" in callPrompts()[1],
                    proposed.steps.map { it.kind },
                    proposed
                        .steps
                        .single()
                        .notes,
                    proposed.summary
                )
            )
        }

    @Test
    fun `a tag the task does not write is held once, and each change added shows the proposal so far`() =
        runTest {
            script(
                """{"tool":"add_tag","notes":["Lease"],"tag":"home"}""",
                """{"tool":"add_tag","notes":["Lease"],"tag":"hogar"}""",
                """{"tool":"propose","summary":"$SUMMARY"}""",
                work = "organize"
            )

            val steps = run(task = "etiqueta el contrato Lease como hogar")

            assertEquals(
                expected = listOf(true, true, "hogar"),
                actual = listOf(
                    "add_tag: not added yet. The task does not write \"home\"." in callPrompts()[1],
                    "The proposal now holds:\n- add_tag, Lease, hogar\n" in callPrompts()[2],
                    (steps.last() as AgentStep.Proposed)
                        .steps
                        .single()
                        .tag
                )
            )
        }

    @Test
    fun `a change refused twice the same way is not offered next, and a field written none is left out`() =
        runTest {
            val tacos = """{"tool":"add_tag","notes":["Tacos"],"tag":"home"}"""

            script(
                tacos,
                tacos,
                """{"tool":"create_note","title":"Rent","template":"none"}""",
                """{"tool":"propose","summary":"$SUMMARY"}""",
                work = "organize"
            )

            val steps = run(task = "tag Tacos as home and create a note Rent")

            val schemas = engine
                .options
                .mapNotNull { it.jsonSchema }
                .filter {
                    "\"propose\"" in it
                }

            assertEquals(
                expected = listOf(true, false, listOf(ActionKind.CREATE_NOTE), null),
                actual = listOf(
                    "was refused the same way again" in callPrompts()[2],
                    "\"add_tag\"" in schemas[2],
                    (steps.last() as AgentStep.Proposed).steps.map { it.kind },
                    (steps.last() as AgentStep.Proposed)
                        .steps
                        .single()
                        .template
                )
            )
        }

    @Test
    fun `a change on a name two notes share is held once, and proposed anyway the person is asked which`() =
        runTest {
            val tag = """{"tool":"add_tag","notes":["Home/Lease"],"tag":"rented"}"""

            script(
                tag,
                tag,
                """{"tool":"propose","summary":"$SUMMARY"}""",
                """{"tool":"ask_user","question":"Which lease?","options":["Home/Lease","Work/Lease"]}""",
                work = "organize"
            )

            val steps = run(
                task = "tag the lease as rented",
                notes = notes + entry(path = office.value)
            )

            assertEquals(
                expected = listOf(true, true, true),
                actual = listOf(
                    "2 notes are named \"Lease\": Home/Lease, Work/Lease. If the task does not" in
                        callPrompts()[1],
                    "(Not done: the task can mean 2 notes and does not say which: Home/Lease, Work/Lease." in
                        callPrompts()[3],
                    steps.last() is AgentStep.Asked
                )
            )
        }

    @Test
    fun `a bare name two notes share is told with their paths, to ask the person or to say which`() =
        runTest {
            store.texts[lease] = "The rent is 950 euros a month."

            val ask =
                """{"tool":"ask_user","question":"Which lease?","options":["Home/Lease","Work/Lease"]}"""

            script("""{"tool":"read","note":"Lease"}""", ask)

            val steps = run(
                task = "summarize the lease",
                notes = notes + entry(path = office.value)
            )

            // Reading them would not say which lease "the lease" is: the question is asked at once.
            assertEquals(
                expected = listOf(true, true),
                actual = listOf(
                    "(2 notes are named \"Lease\": Home/Lease, Work/Lease." in callPrompts()[1],
                    steps.last() is AgentStep.Asked
                )
            )
        }

    @Test
    fun `a call is read from the model's JSON, lists a line an item, escapes undone, and what is no call is none`() {
        assertEquals(
            expected = listOf(
                ToolCall(
                    name = "plan",
                    arguments = mapOf("steps" to "Read \"A\"\nWrite it", "n" to "3")
                ),
                ToolCall(
                    name = "answer",
                    arguments = mapOf("text" to "Línea uno\nlínea dos é")
                ),
                null,
                null
            ),
            actual = listOf(
                ToolCallJson.read(json = """{"tool": "plan", "steps": ["Read \"A\"", "Write it"], "n": 3}"""),
                ToolCallJson.read(json = """{"tool":"answer","text":"Línea uno\nlínea dos é"}"""),
                ToolCallJson.read(json = """{"text":"no tool named"}"""),
                ToolCallJson.read(json = "not json")
            )
        )
    }

    private companion object {
        const val SUMMARY = "Tags the lease."
        const val CALLS = "You work on a task in the person's notes"
    }
}
