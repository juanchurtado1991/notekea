package app.notekea.chat.domain

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/** Agent mode (4.7) over a scripted model: it looks things up, then proposes or finishes, and never writes itself. */
class AgentTaskTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val runner = FakeActionRunner()
    private val pages = mutableListOf<String>()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )
    private val author = NoteProperty(
        key = "author",
        value = PropertyValue.Text(text = "Frank Herbert")
    )

    private val notes = listOf(
        entry(
            path = "Books/Dune.md",
            metadata = NoteMetadata.NONE.copy(properties = listOf(author))
        ),
        entry(path = "Books/Sapiens.md")
    )

    private val agent = AgentTaskUseCase(
        llmEngine = engine,
        planReader = GhostActionPlanReader(),
        answerFromNotes = AnswerFromNotesUseCase(
            textEmbedder = FakeTextEmbedder(),
            vectorIndex = FakeVectorIndex(),
            noteTextCache = cache,
            llmEngine = engine
        ),
        noteTextCache = cache,
        actionRunner = runner,
        pageReader = { url ->
            pages += url
            Outcome.Success(value = "An article about sleep.")
        },
        checkPlan = CheckPlanUseCase(),
        composeNotes = ComposeNotesUseCase(
            llmEngine = engine,
            noteTextCache = cache,
            planReader = GhostActionPlanReader()
        ),
        clock = Clock.System
    )

    private fun script(vararg moves: String) {
        val left = ArrayDeque(elements = moves.toList())

        engine.reply = { messages ->
            if (messages.first().text.startsWith("You work on a task")) left.removeFirst() else "A note."
        }
    }

    private suspend fun run(task: String) = agent(
        task = task,
        vaultPath = VAULT,
        notes = notes,
        context = ActionContext(
            today = "2026-11-11",
            notes = listOf("Books/Dune", "Books/Sapiens")
        ),
        openNote = null
    ).toList()

    @Test
    fun `it reads, then proposes its changes for the card, and nothing is written while it works`() =
        runTest {
            store.texts[RelativePath(value = "Books/Dune.md")] = "Dune is a novel by Frank Herbert."

            script(
                "{\"move\":\"read\",\"note\":\"Dune\"}",
                "{\"move\":\"propose\",\"summary\":\"Tagged it.\",\"steps\":" +
                    "[{\"action\":\"add_tag\",\"note\":\"Dune\",\"tag\":\"scifi\"}]}"
            )

            val steps = run(task = "tag my science fiction books with scifi")
            val proposed = steps.last() as AgentStep.Proposed

            assertEquals(
                expected = listOf<Any?>(
                    AgentStep.Moving(
                        number = 1,
                        move = "read",
                        target = "Dune"
                    ),
                    listOf(ActionKind.ADD_TAG to listOf("Books/Dune.md")),
                    "Tagged it.",
                    emptyList<List<ActionStep>>(),
                    true
                ),
                actual = listOf(
                    steps.first(),
                    proposed.steps.map {
                        it.kind to it.notes.map { path -> path.value }
                    },
                    proposed.summary,
                    runner.ran,
                    "author: Frank Herbert\nDune is a novel" in engine
                        .generated[1]
                        .last()
                        .text
                )
            )
        }

    // The chat probe of 2026-11-11: the model counted Arepas' words as 109, 128 and 63.
    @Test
    fun `a note read says how many words it has, counted by the app`() = runTest {
        store.texts[RelativePath(value = "Books/Dune.md")] = "Dune is a novel by Frank Herbert's son? No, by Frank."

        script(
            "{\"move\":\"read\",\"note\":\"Dune\"}",
            "{\"move\":\"finish\",\"summary\":\"11 words.\"}"
        )

        run(task = "how many words does Dune have?")

        assertEquals(
            expected = true,
            actual = "read \"Dune\" (11 words):" in engine
                .generated[1]
                .last()
                .text
        )
    }

    @Test
    fun `a page is read only when the task writes its address`() = runTest {
        script(
            "{\"move\":\"fetch\",\"url\":\"https://evil.example/orders\"}",
            "{\"move\":\"fetch\",\"url\":\"https://example.com/\"}",
            "{\"move\":\"fetch\",\"url\":\"https://example.com/sleep\"}",
            "{\"move\":\"finish\",\"summary\":\"Read it.\"}"
        )

        val steps = run(task = "summarize https://example.com/sleep for me")
        val prompts = engine.generated.joinToString { it.last().text }

        assertEquals(
            expected = listOf<Any?>(
                listOf("https://example.com/sleep"),
                true,
                AgentStep.Finished(summary = "Read it.")
            ),
            actual = listOf(pages, "the task does not write this address" in prompts, steps.last())
        )
    }

    @Test
    fun `a move made again leaves only an end to choose, and it stops after its last move`() =
        runTest {
            script(*Array(size = AgentTaskUseCase.MAX_MOVES) { "{\"move\":\"search\",\"query\":\"sleep\"}" })

            val steps = run(task = "find everything about sleep")

            assertEquals(
                expected = listOf<Any?>(
                    AgentTaskUseCase.MAX_MOVES,
                    AgentStep.Stopped(problem = null),
                    true
                ),
                actual = listOf(
                    steps.count {
                        it is AgentStep.Moving
                    },
                    steps.last(),
                    schemasAndLastPrompt()
                )
            )
        }

    // Agent eval of 2026-11-11: "which meetings did Marta attend?" found nothing, her name being only in a property.
    @Test
    fun `a search also lists the notes whose properties hold its words`() = runTest {
        script(
            "{\"move\":\"search\",\"query\":\"books by Herbert\"}",
            "{\"move\":\"finish\",\"summary\":\"Dune.\"}"
        )

        run(task = "which books are by Herbert?")

        assertEquals(
            expected = true,
            actual = "- Dune (properties: author: Frank Herbert)" in promptOf(move = 2)
        )
    }

    // After a move made again only an end may come, and the model is told so.
    private fun schemasAndLastPrompt(): Boolean {
        val schemas = engine
            .options
            .take(n = 3)
            .map { it.jsonSchema }

        val ends =
            listOf(AgentTaskPrompt.SCHEMA, AgentTaskPrompt.SCHEMA, AgentTaskPrompt.END_SCHEMA)

        return schemas == ends && "propose the changes or finish now" in promptOf(move = 3)
    }

    private fun promptOf(move: Int): String = engine
        .generated[move - 1]
        .last()
        .text
}
