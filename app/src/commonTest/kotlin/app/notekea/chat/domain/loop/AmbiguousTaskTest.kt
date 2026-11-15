package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.NoteFixtures.entry
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
}
