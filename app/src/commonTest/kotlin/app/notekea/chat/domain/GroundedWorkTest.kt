package app.notekea.chat.domain

import app.notekea.chat.data.GhostTextNotesReader
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.Placeholders
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.plan.UrlsNamed
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.figures.BackedFigures
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmModelInfo
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat's work is written from what it read (the developer's smoke of 2026-11-14: asked for a table of a pasted
 * contract's pros and cons, it read the note that only embeds the document and wrote examples to replace).
 */
class GroundedWorkTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)
    private val note = RelativePath(value = "Contract/contract.md")
    private val document = RelativePath(value = "Contract/terms.docx")

    private val notes = listOf(
        entry(path = note.value),
        entry(path = document.value)
    )

    private val compose = ComposeNotesUseCase(
        llmEngine = engine,
        noteTextCache = cache,
        notesReader = GhostTextNotesReader()
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

    private fun script(written: List<String>) {
        val bodies = ArrayDeque(elements = written)

        engine.reply = { messages ->
            val asked = messages.first().text

            if (asked.startsWith("You write the body")) bodies.removeFirst() else "NOT_FOUND"
        }
    }

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

    // Code review, 2026-11-14: thirty long notes asked for 6,600 tokens of digests, and a long source for more
    // output than the 4,096-token context has left.
    @Test
    fun `many long notes and a long source are written within the model's context`() = runTest {
        val many = (1..30).map { RelativePath(value = "Folder/Note $it.md") }
        many.forEach { store.texts[it] = "A fact about the lease. ".repeat(n = 40) }
        script(written = listOf("A summary of the lease."))

        compose(
            ActionStep(
                kind = ActionKind.WRITE_FROM_NOTES,
                notes = many,
                title = "Summary",
                text = "a summary"
            ),
            "summarize them",
            VAULT,
            many.map { entry(path = it.value) }
        )

        val digests = engine
            .options
            .dropLast(n = 1)
            .sumOf { it.maxTokens }

        val written = engine
            .options
            .last()
            .maxTokens

        val read = engine
            .generated
            .last()
            .sumOf { it.text.length } / 3

        assertEquals(
            expected = listOf(30, true, true),
            actual = listOf(engine.options.size - 1, digests <= 2_200, written + read <= 4_096)
        )
    }

    // `docs/agent-loop.md`, step 2: 12,000 characters are 3,000 tokens, more than a 4,096 context reads at once and
    // well within the 8,192 the loop is given where the machine has them.
    @Test
    fun `a document the model's context holds is read whole, and digested only where it does not fit`() =
        runTest {
            val long = RelativePath(value = "Folder/Long.md")
            store.texts[long] = "A fact about the lease. ".repeat(n = 500)
            script(written = listOf("A digest.", "A digest.", "A summary.", "A summary."))

            val step = ActionStep(
                kind = ActionKind.WRITE_FROM_NOTES,
                notes = listOf(long),
                title = "Summary",
                text = "a summary"
            )

            compose(step, "summarize it", VAULT, listOf(entry(path = long.value)))
            val narrow = engine.generated.size

            engine.readiness = Outcome.Success(
                value = LlmModelInfo(
                    isSmall = false,
                    contextTokens = 16_384
                )
            )

            compose(step, "summarize it", VAULT, listOf(entry(path = long.value)))

            assertEquals(
                expected = listOf(3, 1, true),
                actual = listOf(
                    narrow,
                    engine.generated.size - narrow,
                    engine
                        .generated
                        .last()
                        .last()
                        .text
                        .contains(
                            other = store
                                .texts
                                .getValue(long)
                                .trim()
                        )
                )
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
