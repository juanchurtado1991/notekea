package app.notekea.tree.data

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.tasks.NoteTask
import kotlin.test.Test
import kotlin.test.assertEquals

/** The tasks a note's lines give the cache. */
class NoteTaskCollectorTest {
    private fun tasksOf(text: String): List<NoteTask> = NoteTaskCollector().apply {
        text.lines().forEach { read(line = it) }
    }.tasks

    @Test
    fun `a task knows where it is, its heading and whether it is done`() {
        val text = "# Plan\n\n- [ ] Write\n## Later\n- [x] Ship\n"

        val tasks = tasksOf(text = text)

        assertEquals(
            expected = listOf(
                NoteTask(
                    stateIndex = 11,
                    endIndex = 19,
                    text = "Write",
                    isDone = false,
                    heading = "Plan"
                ),
                NoteTask(
                    stateIndex = 32,
                    endIndex = 39,
                    text = "Ship",
                    isDone = true,
                    heading = "Later"
                )
            ),
            actual = tasks
        )

        assertEquals(
            expected = 'x',
            actual = text[tasks.last().stateIndex]
        )
    }

    @Test
    fun `the Tasks plugin's date wins over a date in the text, and leaves the text`() {
        val tasks = tasksOf(
            text = "- [ ] Call 2026-01-01 📅 2026-11-02\n- [ ] Pay 2026-12-24\n- [ ] Nothing\n"
        )

        assertEquals(
            expected = listOf(
                CalendarDate(
                    year = 2026,
                    month = 11,
                    day = 2
                ),
                CalendarDate(
                    year = 2026,
                    month = 12,
                    day = 24
                ),
                null
            ),
            actual = tasks.map { it.date }
        )

        assertEquals(
            expected = "Call 2026-01-01",
            actual = tasks.first().text
        )
    }

    @Test
    fun `a date that is no date is none, and a task reads as text`() {
        val tasks = tasksOf(
            text = "- [ ] Read [[Dune|the book]] **now** ^shopping\n- [ ] Later 2026-13-45\n"
        )

        assertEquals(
            expected = listOf("Read the book now", "Later 2026-13-45"),
            actual = tasks.map { it.text }
        )

        assertEquals(
            expected = listOf(null, null),
            actual = tasks.map { it.date }
        )
    }

    @Test
    fun `a date in a link, an address or code is no date of the task`() {
        val tasks = tasksOf(
            text = "- [ ] See [[2026-10-24 Budget]]\n- [ ] Read [post](https://a.b/2026-10-24)\n" +
                "- [ ] Run `2026-10-24`\n- [ ] Visit https://a.b/2026-10-24\n"
        )

        assertEquals(
            expected = listOf(null, null, null, null),
            actual = tasks.map { it.date }
        )
    }

    @Test
    fun `a task in a quote or a numbered list reads as its text`() {
        val tasks = tasksOf(text = "> - [ ] Quoted\n1. [ ] Numbered\n")

        assertEquals(
            expected = listOf("Quoted", "Numbered"),
            actual = tasks.map { it.text }
        )
    }

    @Test
    fun `code is not a task, nor a heading`() {
        val tasks = tasksOf(text = "# Real\n```\n# Fake\n- [ ] Code\n```\n- [ ] Text\n")

        assertEquals(
            expected = listOf("Text"),
            actual = tasks.map { it.text }
        )

        assertEquals(
            expected = "Real",
            actual = tasks.single().heading
        )
    }
}
