package app.notekea.chat.domain

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.AgentTaskUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.BackedFigures
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.Placeholders
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
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
 * The chat's work is written from what it read (the developer's smoke of 2026-11-14: asked for a table of a pasted
 * contract's pros and cons, it read the note that only embeds the document and wrote examples to replace).
 */
class GroundedWorkTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )
    private val note = RelativePath(value = "Contract/contract.md")
    private val document = RelativePath(value = "Contract/terms.docx")

    private val notes = listOf(
        entry(path = note.value),
        entry(path = document.value)
    )

    private val compose = ComposeNotesUseCase(
        llmEngine = engine,
        noteTextCache = cache,
        planReader = GhostActionPlanReader()
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
        actionRunner = FakeActionRunner(),
        pageReader = { Outcome.Success(value = "") },
        checkPlan = CheckPlanUseCase(),
        composeNotes = compose,
        clock = Clock.System
    )

    private val planned = listOf(
        PlannedStep(
            kind = ActionKind.WRITE_FROM_NOTES,
            fields = mapOf(
                ActionField.TITLE to "Pros and cons",
                ActionField.TEXT to "a table of pros and cons"
            ),
            notes = listOf("Contract/contract")
        )
    )

    private fun script(
        vararg moves: String,
        written: List<String> = listOf(TABLE)
    ) {
        val left = ArrayDeque(elements = moves.toList())
        val bodies = ArrayDeque(elements = written)

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith("You work on a task") -> left.removeFirst()
                asked.startsWith("You write the body") -> bodies.removeFirst()
                else -> "NOT_FOUND"
            }
        }
    }

    private suspend fun run(planned: List<PlannedStep> = emptyList()) = agent(
        task = "make a note with a table of the pros and cons of the contract I pasted",
        vaultPath = VAULT,
        notes = notes,
        context = ActionContext(
            today = "2026-11-14",
            notes = listOf("Contract/contract")
        ),
        openNote = note,
        planned = planned
    ).toList()

    private fun bodyPrompts(): List<String> = engine
        .generated
        .filter {
            it
                .first()
                .text
                .startsWith(prefix = "You write the body")
        }
        .map { messages ->
            messages.joinToString(separator = "\n") { it.text }
        }

    @Test
    fun `a note read comes with the text of the document pasted in it`() = runTest {
        store.texts[note] = "![[terms.docx]]"
        store.texts[document] = TERMS

        script(
            "{\"move\":\"read\",\"note\":\"contract\"}",
            "{\"move\":\"finish\",\"summary\":\"Done.\"}"
        )

        run()

        assertEquals(
            expected = true,
            actual = TERMS in engine
                .generated[1]
                .last()
                .text
        )
    }

    @Test
    fun `a note it creates after reading is written from what it read, not from what the model knows`() =
        runTest {
            store.texts[note] = "![[terms.docx]]"
            store.texts[document] = TERMS

            script(
                "{\"move\":\"read\",\"note\":\"contract\"}",
                "{\"move\":\"propose\",\"steps\":[{\"action\":\"create_note\",\"title\":\"Pros and cons\"," +
                    "\"text\":\"| Pros | Cons |\\n| (Insert pros) | (Insert cons) |\"}]}"
            )

            val proposed = run().last() as AgentStep.Proposed

            assertEquals(
                expected = listOf(ActionKind.WRITE_FROM_NOTES, listOf(note), true, true),
                actual = listOf(
                    proposed
                        .steps
                        .single()
                        .kind,
                    proposed
                        .steps
                        .single()
                        .notes,
                    TERMS in bodyPrompts().single(),
                    "(Insert pros)" !in bodyPrompts().single()
                )
            )
        }

    // Code review, 2026-11-14: a note asked for empty was filled from whatever the agent had read.
    @Test
    fun `a note asked for with nothing to write stays empty, whatever was read on the way`() =
        runTest {
            store.texts[note] = TERMS

            script(
                "{\"move\":\"read\",\"note\":\"contract\"}",
                "{\"move\":\"propose\",\"steps\":[{\"action\":\"create_note\",\"title\":\"Shopping\"}]}"
            )

            val proposed = run().last() as AgentStep.Proposed

            assertEquals(
                expected = listOf<Any>(ActionKind.CREATE_NOTE, emptyList<String>()),
                actual = listOf<Any>(
                    proposed
                        .steps
                        .single()
                        .kind,
                    bodyPrompts()
                )
            )
        }

    @Test
    fun `asked to write a note, it ends on the card even when the model ends without a proposal`() =
        runTest {
            store.texts[note] = "![[terms.docx]]"
            store.texts[document] = TERMS

            script(
                "{\"move\":\"read\",\"note\":\"contract\"}",
                "{\"move\":\"finish\",\"summary\":\"It cannot be done.\"}"
            )

            val proposed = run(planned = planned).last() as AgentStep.Proposed

            assertEquals(
                expected = "Contract/Pros and cons.md" to true,
                actual = proposed
                    .steps
                    .single()
                    .content
                    .single()
                    .path
                    .value to
                    (
                        TABLE in proposed
                            .steps
                            .single()
                            .content
                            .single()
                            .text
                        )
            )
        }

    // Code review, 2026-11-14: a link to a note named with a dot, and a picture, were said to be attached files
    // with no text; and a clipped page was read from the notes a model wrote about it.
    @Test
    fun `a read names only documents as unread, and reads a clipped page from its own text`() =
        runTest {
            store.texts[note] = "See [[Dr. Who]] and ![[photo.png]] and ![[scan.pdf]].\n\n" +
                "The model's notes.\n\n## Original text\n\nThe page's own words."

            script(
                "{\"move\":\"read\",\"note\":\"contract\"}",
                "{\"move\":\"finish\",\"summary\":\"Done.\"}"
            )

            run()

            val read = engine
                .generated[1]
                .last()
                .text

            assertEquals(
                expected = listOf(true, false, false, false, true),
                actual = listOf(
                    "The page's own words." in read,
                    "The model's notes." in read,
                    "Dr. Who" in read,
                    "photo.png" in read,
                    "It attaches scan.pdf" in read
                )
            )
        }

    @Test
    fun `with no text to read it says so and writes no note`() = runTest {
        store.texts[note] = "![[scan.png]]"

        script(
            "{\"move\":\"read\",\"note\":\"contract\"}",
            "{\"move\":\"propose\",\"steps\":[{\"action\":\"create_note\",\"title\":\"Pros and cons\"," +
                "\"text\":\"a table\"}]}"
        )

        assertEquals(
            expected = AgentStep.Stopped(problem = ActionProblem.NothingToRead(names = listOf("contract"))),
            actual = run().last()
        )
    }

    @Test
    fun `a body of placeholders is written again, and refused when it comes back the same`() =
        runTest {
            store.texts[note] = TERMS
            script(written = listOf(FILLER, TABLE))

            val step = ActionStep(
                kind = ActionKind.WRITE_FROM_NOTES,
                notes = listOf(note),
                title = "Pros and cons",
                text = "a table"
            )

            val first = compose(step, "a table of the contract", VAULT, notes)
            script(written = listOf(FILLER, FILLER))
            val second = compose(step, "a table of the contract", VAULT, notes)

            assertEquals(
                expected = listOf(true, Outcome.Failure(error = ActionProblem.NotBacked)),
                actual = listOf(
                    TABLE in (first as Outcome.Success)
                        .value
                        .content
                        .single()
                        .text,
                    second
                )
            )
        }

    @Test
    fun `a figure the notes do not write has the body written again, told which`() = runTest {
        store.texts[note] = TERMS
        script(written = listOf("The deposit is 4500 dollars.", TABLE))

        compose(
            ActionStep(
                kind = ActionKind.WRITE_FROM_NOTES,
                notes = listOf(note),
                title = "Pros and cons",
                text = "a table"
            ),
            "a table of the contract",
            VAULT,
            notes
        )

        assertEquals(
            expected = true,
            actual = "figures the notes do not write: 4500" in bodyPrompts().last()
        )
    }

    @Test
    fun `placeholders and unbacked figures are told from a text taken from the notes`() {
        assertEquals(
            expected = listOf(true, true, true, false, listOf("4500"), emptyList<String>()),
            actual = listOf(
                Placeholders.isIn(text = "| (Insertar pros de contrato) | (Insertar contras) |"),
                Placeholders.isIn(text = "Reemplaza los ejemplos con los puntos reales de tu documento."),
                Placeholders.isIn(text = "Primero debes abrir el PDF y leer las cláusulas."),
                Placeholders.isIn(text = TABLE),
                BackedFigures.missingIn(
                    text = "It lasts 12 months and costs 4500.",
                    sources = listOf(TERMS)
                ),
                BackedFigures.missingIn(
                    text = "Ceilings of 2,6 m, 12 months, 3 rooms.",
                    sources = listOf("The ceiling is 2.60 m high. $TERMS")
                )
            )
        )
    }

    @Test
    fun `a file of the vault planned as a page to clip is left to the step that reads it`() {
        val clip = PlannedStep(
            kind = ActionKind.CLIP_URL,
            fields = mapOf(ActionField.URL to "Contract/terms.docx")
        )

        assertEquals(
            expected = planned,
            actual = UrlsNamed.shaped(
                steps = listOf(clip) + planned,
                request = "a table of the contract I pasted"
            )
        )
    }

    private companion object {
        const val TERMS = "The lease lasts 12 months. Rent is 950 dollars a month, paid on day 5."
        const val TABLE =
            "| Pros | Cons |\n| --- | --- |\n| A lease of 12 months | Rent of 950 dollars |"
        const val FILLER =
            "| Pros | Cons |\n| --- | --- |\n| (Insert the pros here) | (Insert the cons here) |"
    }
}
