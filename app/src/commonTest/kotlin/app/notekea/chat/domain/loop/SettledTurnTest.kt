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
import app.notekea.core.notes.domain.TextFolder
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
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    private val runTurn = RunTurnUseCase(
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
        clock = Clock.System
    )

    @Test
    fun `the note a word of the task tells apart is said first, and an answer from the other one is held back`() =
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
                calls = listOf(
                    """{"tool":"read","note":"People/Tomás Herrera"}""",
                    """{"tool":"answer","text":"8890-3317"}""",
                    """{"tool":"read","note":"People/Tomás Vargas"}""",
                    """{"tool":"answer","text":"8712-6650"}"""
                )
            )

            assertEquals(
                expected = listOf(true, true),
                actual = listOf(
                    "- People/Tomás Vargas (only it writes \"club\")" in prompts
                        .first()
                        .first()
                        .text,
                    "Read People/Tomás Vargas and answer from it." in prompts[2].last().text
                )
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
        calls: List<String>
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

        runTurn(
            task = task,
            vaultPath = VAULT,
            notes = notes,
            context = ActionContext(
                today = "2026-11-16",
                notes = notes.map {
                    it
                        .file
                        .path
                        .value
                        .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
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
