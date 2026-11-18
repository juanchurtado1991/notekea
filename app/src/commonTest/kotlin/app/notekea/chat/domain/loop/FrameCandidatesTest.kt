package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The notes and entries the frame's model is shown (`docs/agent-frame.md`): found by the code, with no word list
 * about what the task means; the model only says which of them it means.
 */
class FrameCandidatesTest {
    private val notes = listOf(
        "Home/Budget.md",
        "Work/Budget.md",
        "Recipes/Arepas.md",
        "People/Ana Ruiz.md",
        "People/Ana Torres.md",
        "Health/Dentist.md"
    ).map { entry(path = it) }

    private fun pathsOf(
        task: String,
        found: List<String> = emptyList(),
        first: List<String> = emptyList()
    ) = FrameCandidates
        .pathsOf(
            task = task,
            notes = notes,
            found = found.map { RelativePath(value = it) },
            first = first.map { RelativePath(value = it) }
        )
        .map { it.value }

    @Test
    fun `notes named by a word of the task are shown with every note of the same name`() {
        assertEquals(
            expected = listOf(
                listOf("Home/Budget.md", "Work/Budget.md"),
                listOf("People/Ana Ruiz.md", "People/Ana Torres.md")
            ),
            actual = listOf(
                pathsOf(task = "what's left in the budgets?"),
                pathsOf(task = "what is Ana's phone?")
            )
        )
    }

    @Test
    fun `the memory's notes come first, then the named ones, then those the search found, and a namesake follows`() {
        assertEquals(
            expected = listOf("Health/Dentist.md", "Recipes/Arepas.md", "Home/Budget.md", "Work/Budget.md"),
            actual = pathsOf(
                task = "move my appointment",
                found = listOf("Recipes/Arepas.md", "Home/Budget.md"),
                first = listOf("Health/Dentist.md")
            )
        )
    }

    @Test
    fun `entries are the list lines a task word touches, or every line under a heading it touches`() {
        val text = """
            # Dentist
            Dr. Paz, Calle 4.

            ## Appointments
            - 2026-12-01 10:00 cleaning
            - 2026-12-09 16:30 filling

            ## Bills
            | Date | Paid |
            |---|---|
            | 2026-10-02 | 40 |
            - insurance card renewed
        """.trimIndent()

        assertEquals(
            expected = listOf(
                listOf("- 2026-12-01 10:00 cleaning", "- 2026-12-09 16:30 filling"),
                listOf("| Date | Paid |", "| 2026-10-02 | 40 |", "- insurance card renewed")
            ),
            actual = listOf(
                FrameCandidates.entriesOf(
                    task = "change the appointment to 11:00",
                    text = text
                ),
                FrameCandidates.entriesOf(
                    task = "which bills did I pay?",
                    text = text
                )
            )
        )
    }
}
