package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.Remembering
import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** The notes a task can mean and does not say which of, for names the evaluation's vault does not hold. */
class AmbiguousTaskTest {
    private val notes = listOf(
        entry(path = "People/Ángela Ruiz.md"),
        entry(path = "People/Ángela Soto.md"),
        entry(path = "People/José Mora.md"),
        entry(path = "People/José Vega.md")
    )

    private fun heldOf(task: String) = AmbiguousTask
        .notesOf(
            task = task,
            notes = notes
        )
        .map { it.value }

    // Code review, 2026-11-15: a word boundary read as ASCII missed "Ángela" and cut "José" to "Jos".
    @Test
    fun `a first name that starts or ends with an accent is a name two people have`() {
        assertEquals(
            expected = listOf(
                listOf("People/Ángela Ruiz.md", "People/Ángela Soto.md"),
                listOf("People/José Mora.md", "People/José Vega.md")
            ),
            actual = listOf(
                heldOf(task = "call Ángela tomorrow"),
                heldOf(task = "¿cuál es el correo de José?")
            )
        )
    }

    // docs/agent-memory.md, "How a line is written": the answer chosen becomes a line the app builds, not the model.
    @Test
    fun `the words that meant the notes become the line kept for the answer chosen, a shared title by its path`() {
        val angela = notes.take(n = 2).map { it.file.path }

        val budgets = listOf("Home/Budget 2026.md", "Work/Budget 2026.md").map {
            RelativePath(value = it)
        }

        val said = AmbiguousTask.saidOf(
            task = "what is the total of the budget?",
            paths = budgets
        )

        assertEquals(
            expected = listOf(
                "\"Ángela\" is [[Ángela Ruiz]]",
                "the budget",
                "\"the budget\" is [[Work/Budget 2026]]"
            ),
            actual = listOf(
                Remembering(
                    said = AmbiguousTask.saidOf(
                        task = "call Ángela tomorrow",
                        paths = angela
                    ).orEmpty(),
                    notes = angela
                ).lineFor(option = "Ángela Ruiz"),
                said,
                Remembering(
                    said = said.orEmpty(),
                    notes = budgets
                ).lineFor(option = "Work/Budget 2026")
            )
        )
    }

    // docs/agent-memory.md: a name the memory says the note of is settled, and only that name.
    @Test
    fun `a name the memory settles is not held, a name it does not settle still is`() {
        val settled = listOf(
            MemoryLine.Named(
                said = "Ángela",
                note = "People/Ángela Ruiz"
            )
        )

        val held = listOf("call Ángela tomorrow", "¿cuál es el correo de José?").map {
            AmbiguousTask
                .notesOf(
                    task = it,
                    notes = notes,
                    settled = settled
                )
                .map { path -> path.value }
        }

        assertEquals(
            expected = listOf(
                emptyList(),
                listOf("People/José Mora.md", "People/José Vega.md")
            ),
            actual = held
        )
    }

    // The bug hunt of 2026-11-17, L9: "Cuál" after "¿" was counted as a second name, so José was never alone.
    @Test
    fun `a capital that opens a sentence after an inverted mark is no name`() {
        assertEquals(
            expected = listOf(
                listOf("People/José Mora.md", "People/José Vega.md"),
                listOf("People/Ángela Ruiz.md", "People/Ángela Soto.md")
            ),
            actual = listOf(
                heldOf(task = "¿Cuál es el correo de José?"),
                heldOf(task = "¡Llama a Ángela hoy!")
            )
        )
    }

    // L13: "two" in the quoted text switched the check off; "the analysis" was never "the X" for ending in "s".
    @Test
    fun `a plural word inside quoted text, and a singular word ending in s, leave the task about one note`() {
        val goals = listOf("Personal/Goals 2027.md", "Work/Goals 2027.md").map { entry(path = it) }

        val analyses = listOf("Reports/Sales analysis.md", "Reports/Cost analysis.md").map {
            entry(path = it)
        }

        val held = listOf(
            "add 'read two books' to Goals 2027" to goals,
            "summarize the analysis" to analyses
        ).map { (task, notes) ->
            AmbiguousTask
                .notesOf(
                    task = task,
                    notes = notes
                )
                .size
        }

        assertEquals(
            expected = listOf(2, 2),
            actual = held
        )
    }

    // S2: "in three bullets" and a new sentence's capital dropped the memory's note as if the task said which.
    @Test
    fun `a common word or a new sentence after the memory's name says nothing of which one`() {
        val qualified = listOf(
            "Summarize my goals in three bullets" to "my goals",
            "Tell me about Laura. Summarize it." to "Laura",
            "el correo de Laura de Contabilidad" to "Laura",
            "call Laura Méndez" to "Laura",
            "the phone of Tomás from the chess club" to "Tomás"
        ).map { (task, said) ->
            AmbiguousTask.isQualified(
                task = task,
                said = said
            )
        }

        assertEquals(
            expected = listOf(false, false, true, true, true),
            actual = qualified
        )
    }

    // L10: a task about one note may not be settled by an end that names every note it could mean.
    @Test
    fun `a task about one thing is singular, quoting a plural word too, and one about several is not`() {
        assertEquals(
            expected = listOf(true, true, false, false),
            actual = listOf(
                "what is Laura's phone?",
                "add 'call both parents' to Goals 2027",
                "compare the budgets",
                "summarize both leases"
            ).map { AmbiguousTask.isSingular(task = it) }
        )
    }
}
