package app.notekea.chat.domain.loop

import app.notekea.core.memory.domain.MemoryLine
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteFixtures.tags
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The note a task means among several of one name, settled before the turn: by every name a line of the memory
 * says, and by a word of the task only one of them writes. The loop's fifth run (2026-11-16) asked which Goals 2027
 * for "my goals", which the memory says, and answered "Tomás from the chess club" with the first Tomás it read.
 */
class SettledNotesTest {
    private val notes = listOf(
        entry(path = "Personal/Goals 2027.md"),
        entry(path = "Work/Goals 2027.md"),
        entry(path = "Personal/Weekly review.md"),
        entry(path = "Work/Weekly review.md")
    )

    private val line = MemoryLine(
        section = MemorySection.PEOPLE,
        text = "\"the weekly review\" and \"la revisión semanal\" are [[Work/Weekly review]]",
        origin = null
    )

    private val goals = MemoryLine(
        section = MemorySection.PEOPLE,
        text = "\"my goals\" are [[Work/Goals 2027]]",
        origin = null
    )

    private fun meantOf(task: String) = AmbiguousTask
        .settledOf(
            task = task,
            notes = notes,
            settled = (line.names + goals.names)
        )
        .map { it.meant.value }

    @Test
    fun `a line of the memory settles every name it says`() {
        assertEquals(
            expected = listOf("the weekly review", "la revisión semanal"),
            actual = line.names.map { it.said }
        )
    }

    @Test
    fun `the memory settles a shared name the task's own words find no set for, unless the task says which`() {
        assertEquals(
            expected = listOf(
                listOf("Work/Goals 2027.md"),
                listOf("Work/Weekly review.md"),
                emptyList()
            ),
            actual = listOf(
                meantOf(task = "add 'learn to play the guitar' to my goals"),
                meantOf(task = "añade 'revisar el correo' a la revisión semanal"),
                meantOf(task = "add 'run 10 km' to my personal goals for 2027")
            )
        )
    }

    @Test
    fun `a word of the task only one note writes says which note, and what the task quotes does not`() {
        val reviews = notes.takeLast(n = 2)

        val people = listOf(
            entry(path = "People/Tomás Herrera.md"),
            entry(
                path = "People/Tomás Vargas.md",
                metadata = tags("people", "club")
            )
        )

        val texts = mapOf(
            reviews[0].file.path to "Every Sunday evening, twenty minutes.\n- Water the plants",
            reviews[1].file.path to "Every Friday at 16:00, before leaving.\n- Update the deliveries sheet",
            people[0].file.path to "Friend from university. phone: 8890-3317",
            people[1].file.path to "Plays on Thursdays. phone: 8712-6650"
        )

        val meant = listOf(
            "add 'water the plants twice' to my Sunday weekly review" to reviews,
            "add 'update the deliveries sheet' to the weekly review" to reviews,
            "what is the phone number of Tomás from the chess club?" to people,
            "what is Tomás's phone number?" to people
        ).map { (task, group) ->
            ToldApart
                .settledOf(
                    task = task,
                    groups = listOf(group),
                    texts = texts
                )
                .map { "${it.meant.value}: ${it.said}" }
        }

        assertEquals(
            expected = listOf(
                listOf("Personal/Weekly review.md: sunday"),
                emptyList(),
                listOf("People/Tomás Vargas.md: club"),
                emptyList()
            ),
            actual = meant
        )
    }
}
