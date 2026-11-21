package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.web.WebSearchTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * Bugs of the loop's code found by the bug hunt of 2026-11-17, each driven through a turn: questions the loop itself
 * asked for held back, a folder the task names ignored, an end naming every note a task about one could mean, a
 * refused change ending a turn that answered, and a refused call offered again at once.
 */
class BugHuntTurnTest {
    private val engine = FakeLlmEngine()

    private val cache = NoteTextCache(
        contentStore = FakeNoteContentStore(),
        textFolder = TextFolder()
    )

    private val searched = mutableListOf<String>()

    // The frame a test gives its turn, as the frame's model would say it; none when a test gives none.
    private val frames = mutableListOf<TaskFrame>()

    private val webSearch = WebSearchTool { query, _ ->
        searched += query
        Outcome.Success(value = emptyList())
    }

    private val runTurn = RunTurnUseCase(
        llmEngine = engine,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ASK to listOf(
                    ReadTool(noteTextCache = cache),
                    webSearch,
                    ChangeTools.append,
                    ChangeTools.addTag,
                    ChangeTools.addTask,
                    AskUserTool(),
                    ProposeTool(),
                    AnswerTool()
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
        frames = FrameSource { _, _, _, _, _, _ -> frames.lastOrNull() }
    )

    private fun frameOf(
        count: FrameCount,
        meant: List<String>,
        shown: List<String>
    ) = TaskFrame(
        targets = meant.map { FrameTarget(note = RelativePath(value = it)) },
        count = count,
        candidates = shown.map { RelativePath(value = it) }
    )

    // The model makes [calls] in turn, then answers; every answer written is [WRITTEN].
    private suspend fun run(
        task: String,
        paths: List<String>,
        calls: List<String>
    ): List<AgentStep> {
        val left = ArrayDeque(elements = calls)

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> ROUTE
                asked.startsWith(prefix = CALLS) -> left.removeFirstOrNull() ?: ANSWER
                else -> WRITTEN
            }
        }

        return runTurn(
            task = task,
            vaultPath = VAULT,
            notes = paths.map { entry(path = it) },
            context = ActionContext(
                today = "2026-11-17",
                notes = paths.map { it.removeSuffix(suffix = ".md") }
            ),
            openNote = null
        ).toList()
    }

    private fun callPrompts(): List<String> = engine
        .generated
        .filter {
            it
                .first()
                .text
                .startsWith(prefix = CALLS)
        }
        .map { it.last().text }

    private fun callSchemas(): List<String> = engine
        .options
        .mapNotNull { it.jsonSchema }
        .filter {
            "\"tool\"" in it
        }

    // L4: "2 notes are named Goals 2027 … ask_user with them as the options", then the question held as unread.
    @Test
    fun `the question the loop asked for, the paths of one shared name, is asked`() = runTest {
        val steps = run(
            task = "what do I want to do next year?",
            paths = listOf("Personal/Goals 2027.md", "Work/Goals 2027.md"),
            calls = listOf(
                """{"tool":"read","note":"Goals 2027"}""",
                """{"tool":"ask_user","question":"Which?","options":["Personal/Goals 2027","Work/Goals 2027"]}"""
            )
        )

        assertEquals(
            expected = true,
            actual = steps.last() is AgentStep.Asked
        )
    }

    // L5: "the work budget" with Home/Budget 2026 and Work/Budget 2026 was told to ask which.
    @Test
    fun `a shared name the frame says which of is told to be called again with that path`() =
        runTest {
            val budgets = listOf("Home/Budget 2026.md", "Work/Budget 2026.md")

            frames += frameOf(
                FrameCount.ONE,
                meant = budgets.drop(n = 1),
                shown = budgets
            )

            run(
                task = "what is the total of the work budget?",
                paths = listOf("Home/Budget 2026.md", "Work/Budget 2026.md"),
                calls = listOf("""{"tool":"read","note":"Budget 2026"}""")
            )

            assertEquals(
                expected = true,
                actual = "The task means Work/Budget 2026: call again with that whole path" in
                    callPrompts()[1]
            )
        }

    // L10, the loop's eighth run: "Laura's phone" answered with both Lauras' phones.
    @Test
    fun `a task about one note that ends naming every note it could mean is held for the question`() =
        runTest {
            val lauras = listOf("People/Laura Castro.md", "People/Laura Méndez.md")

            frames += frameOf(
                FrameCount.ONE,
                meant = lauras,
                shown = lauras
            )

            val steps = run(
                task = "what is the phone of Laura?",
                paths = listOf("People/Laura Castro.md", "People/Laura Méndez.md"),
                calls = listOf(
                    """{"tool":"answer","text":"Laura Castro: 8701-9032. Laura Méndez: 8712-6650."}""",
                    """{"tool":"ask_user","question":"Which Laura?","options":["Laura Castro","Laura Méndez"]}"""
                )
            )

            assertEquals(
                expected = true,
                actual = steps.last() is AgentStep.Asked
            )
        }

    // The plan's step 2.4: the frame open before the first step, the question is asked at once, before any step can
    // use the turn up, and its options are the frame's.
    @Test
    fun `a frame open before the first step asks at once, with the frame's options`() = runTest {
        val lauras = listOf("People/Laura Castro.md", "People/Laura Méndez.md")

        frames += frameOf(
            FrameCount.ONE,
            meant = lauras,
            shown = lauras
        )

        val steps = run(
            task = "what is the phone of Laura?",
            paths = lauras,
            calls = listOf("""{"tool":"ask_user","question":"Which Laura?","options":["Laura"]}""")
        )

        val asked = steps.last() as? AgentStep.Asked

        assertEquals(
            expected = listOf(true, 0, listOf("People/Laura Castro", "People/Laura Méndez")),
            actual = listOf(
                asked != null,
                steps.count {
                    it is AgentStep.Moving
                },
                asked?.options
            )
        )
    }

    // L12: a change tool's "ask_user when the task does not say", then the question held as needless.
    @Test
    fun `a question a refused change told the model to ask is not held back`() = runTest {
        val steps = run(
            task = "tag Arepas with latin",
            paths = listOf("Recipes/Arepas.md", "Recipes/Black beans.md"),
            calls = listOf(
                """{"tool":"add_tag","notes":"Recipes/Nowhere","tag":"latin"}""",
                """{"tool":"ask_user","question":"Which tag?","options":["latin","latina"]}"""
            )
        )

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(
                steps.last() is AgentStep.Asked,
                callPrompts().any {
                    "(Not asked" in it
                }
            )
        )
    }

    // L14: a web turn's refused append, then the right answer, ended as "No change was added".
    @Test
    fun `an answer after a refused change is kept, unless it says a change was made`() = runTest {
        val append = """{"tool":"append","note":"Recipes/Nowhere","text":"Corn flour."}"""

        val answers = listOf("Arepas are made of corn flour.", "I added corn flour to the note.")

        val written = answers.map {
            engine.generated.clear()

            run(
                task = "what are arepas made of?",
                paths = listOf("Recipes/Arepas.md"),
                calls = listOf(append, """{"tool":"answer","text":"$it"}""")
            )

            engine
                .generated
                .last()
                .joinToString(separator = "\n") { message -> message.text }
        }

        assertEquals(
            expected = listOf(false, true),
            actual = written.map {
                TurnEnd.NOTHING_CHANGED in it
            }
        )
    }

    // E3: at temperature 0 the 4B made one refused call four times running.
    @Test
    fun `a refused change's tool is not offered until another call is made`() = runTest {
        run(
            task = "tag arepas with latin and add a task to buy corn",
            paths = listOf("Recipes/Arepas.md"),
            calls = listOf(
                """{"tool":"add_tag","notes":"Recipes/Nowhere","tag":"latin"}""",
                """{"tool":"add_task","note":"Recipes/Arepas","text":"Buy corn"}"""
            )
        )

        assertEquals(
            expected = listOf(true, false, true),
            actual = callSchemas()
                .take(n = 3)
                .map {
                    TAGGING in it
                }
        )
    }

    // L6: `\b` is ASCII on the JVM, so a note whose title starts with "Á" was never seen in a search.
    @Test
    fun `a person's details searched by a title that starts with an accent are refused`() =
        runTest {
            run(
                task = "find Ángela's phone",
                paths = listOf("People/Ángela Ruiz.md"),
                calls = listOf("""{"tool":"web_search","query":"teléfono de Ángela Ruiz"}""")
            )

            assertEquals(
                expected = listOf(true, emptyList<String>()),
                actual = listOf(
                    "a person's phone, address or email is never looked up" in callPrompts()[1],
                    searched
                )
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ROUTE = """{"work":"ask","skill":"none"}"""
        const val ANSWER = """{"tool":"answer","text":"Done."}"""
        const val WRITTEN = "The answer."
        const val TAGGING = "\"const\":\"add_tag\""
    }
}
