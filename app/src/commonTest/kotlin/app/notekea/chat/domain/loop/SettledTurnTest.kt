package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteFixtures.tags
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * A turn told which note its task means, and held back when it goes against it (the loop's fifth run, 2026-11-16):
 * the 4B answered "Tomás from the chess club" with the first Tomás it read, and asked "which recipe? Options:
 * Pancakes" with the one recipe found.
 */
class SettledTurnTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)

    private fun runTurn(frame: TaskFrame?) = RunTurnUseCase(
        llmEngine = engine,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ASK to listOf(
                    ReadTool(noteTextCache = cache),
                    AskUserTool(),
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
        frames = frame?.let {
            FrameSource { _, _, _, _, _, _ -> it }
        }
    )

    private val herrera = RelativePath(value = "People/Tomás Herrera.md")
    private val vargas = RelativePath(value = "People/Tomás Vargas.md")

    // What the frame's model says of "Tomás from the chess club": Vargas, of the two Tomás it was shown.
    private val chessClub = TaskFrame(
        targets = listOf(FrameTarget(note = vargas)),
        count = FrameCount.ONE,
        candidates = listOf(herrera, vargas)
    )

    @Test
    fun `the note the frame says is told first, and an answer from the other one is held back`() =
        runTest {
            val people = listOf(
                entry(path = "People/Tomás Herrera.md"),
                entry(
                    path = "People/Tomás Vargas.md",
                    metadata = tags("people", "club")
                )
            )

            store.texts[people[0].file.path] = "# Tomás Herrera\n\nphone: 8890-3317"
            store.texts[people[1].file.path] = "# Tomás Vargas\n\nphone: 8712-6650"

            val prompts = promptsOf(
                task = "what is the phone number of Tomás from the chess club?",
                notes = people,
                frame = chessClub,
                calls = listOf(
                    """{"tool":"read","note":"People/Tomás Herrera"}""",
                    """{"tool":"answer","text":"8890-3317"}""",
                    """{"tool":"read","note":"People/Tomás Vargas"}""",
                    """{"tool":"answer","text":"8712-6650"}""",
                    """{"tool":"answer","text":"8712-6650"}"""
                )
            )

            assertEquals(
                expected = listOf(true, true),
                actual = listOf(
                    "The task means People/Tomás Vargas. Work on it" in prompts
                        .first()
                        .first()
                        .text,
                    "you have not read it. Read it and answer from it." in prompts[2].last().text
                )
            )
        }

    // The run after the bug hunt: "the budget" answered with both budgets, though one was the note meant.
    @Test
    fun `an answer after reading the note meant and its namesake is told to keep to the one meant`() =
        runTest {
            val budgets = listOf("Home/Budget 2026.md", "Work/Budget 2026.md").map {
                entry(path = it)
            }

            store.texts[budgets[0].file.path] = "# Budget 2026\n\n9,600 USD in all."
            store.texts[budgets[1].file.path] = "# Budget 2026\n\n68,000 USD in all."

            val prompts = promptsOf(
                task = "what's the total of the household budget?",
                notes = budgets,
                frame = TaskFrame(
                    targets = listOf(FrameTarget(note = budgets[0].file.path)),
                    count = FrameCount.ONE,
                    candidates = budgets.map { it.file.path }
                ),
                calls = listOf(
                    """{"tool":"read","note":"Home/Budget 2026"}""",
                    """{"tool":"read","note":"Work/Budget 2026"}""",
                    """{"tool":"answer","text":"9,600 or 68,000 USD"}""",
                    """{"tool":"answer","text":"9,600 USD"}"""
                )
            )

            assertEquals(
                expected = true,
                actual = "Answer from it alone" in prompts[3].last().text
            )
        }

    // The run after the bug hunt: asked "the chess one or the other?", options that name no note.
    @Test
    fun `a question the note meant already answers is not asked, whatever its options say`() =
        runTest {
            val people = listOf(
                entry(path = "People/Tomás Herrera.md"),
                entry(
                    path = "People/Tomás Vargas.md",
                    metadata = tags("people", "club")
                )
            )

            store.texts[people[1].file.path] = "# Tomás Vargas\n\nphone: 8712-6650"

            val prompts = promptsOf(
                task = "what is the phone number of Tomás from the chess club?",
                notes = people,
                frame = chessClub,
                calls = listOf(
                    """{"tool":"read","note":"People/Tomás Vargas"}""",
                    """{"tool":"ask_user","question":"Which?","options":["The chess one","The other"]}""",
                    """{"tool":"answer","text":"8712-6650"}"""
                )
            )

            assertEquals(
                expected = true,
                actual = "Not asked: the task names what it means" in prompts[2].last().text
            )
        }

    // The run after the bug hunt: "which of my two budgets is larger?" answered from one, "there is no second".
    @Test
    fun `an answer about several notes of one name waits for the unread one`() = runTest {
        val budgets = listOf("Home/Budget 2026.md", "Work/Budget 2026.md").map { entry(path = it) }
        store.texts[budgets[0].file.path] = "# Budget 2026\n\n9,600 USD in all."
        store.texts[budgets[1].file.path] = "# Budget 2026\n\n68,000 USD in all."

        val prompts = promptsOf(
            task = "which of my two budgets is larger, and by how much?",
            notes = budgets,
            frame = TaskFrame(
                targets = budgets.map { FrameTarget(note = it.file.path) },
                count = FrameCount.SEVERAL,
                candidates = budgets.map { it.file.path }
            ),
            calls = listOf(
                """{"tool":"read","note":"Home/Budget 2026"}""",
                """{"tool":"answer","text":"There is no second budget."}""",
                """{"tool":"answer","text":"The work one is larger."}"""
            )
        )

        assertEquals(
            expected = true,
            actual = "you have not read Work/Budget 2026" in prompts[2].last().text
        )
    }

    @Test
    fun `a question with one note as its only answer is not asked`() = runTest {
        val recipe = entry(path = "Recipes/Pancakes.md")
        store.texts[recipe.file.path] = "# Pancakes\n\nservings: 2"

        val prompts = promptsOf(
            task = "which recipe makes two servings?",
            notes = listOf(recipe),
            calls = listOf(
                """{"tool":"read","note":"Recipes/Pancakes"}""",
                """{"tool":"ask_user","question":"Which recipe?","options":["Pancakes"]}""",
                """{"tool":"answer","text":"Pancakes"}"""
            )
        )

        assertEquals(
            expected = true,
            actual = "one note is nothing to choose between. Do the task with Recipes/Pancakes now." in
                prompts[2].last().text
        )
    }

    private suspend fun promptsOf(
        task: String,
        notes: List<NoteEntry>,
        calls: List<String>,
        frame: TaskFrame? = null
    ): List<List<ChatMessage>> {
        val left = ArrayDeque(elements = calls)

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> ROUTE
                asked.startsWith(prefix = CALLS) -> left.removeFirst()
                else -> "The answer."
            }
        }

        runTurn(frame = frame)(
            task = task,
            vaultPath = VAULT,
            notes = notes,
            context = ActionContext(
                today = "2026-11-16",
                notes = notes.map {
                    it
                        .file
                        .path
                        .bare()
                }
            ),
            openNote = null
        ).toList()

        return engine.generated.filter {
            it
                .first()
                .text
                .startsWith(prefix = CALLS)
        }
    }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ROUTE = """{"work":"ask","skill":"none"}"""
    }
}
