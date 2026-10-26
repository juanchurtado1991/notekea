package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TaskFinderTest {
    private val finder = TaskFinder()

    @Test
    fun `tasks are found in every kind of list, quotes included, checked or not`() {
        val text = "- [ ] Buy flour\n* [x] Soak beans\n+ [X] Done\n1. [ ] First\n  - [ ] Nested\n> - [ ] In a quote\n"

        assertEquals(
            expected = listOf(false, true, true, false, false, false),
            actual = finder.tasks(text = text).map { it.isDone }
        )
        assertEquals(
            expected = List(size = 6) { '[' },
            actual = finder.tasks(text = text).map { text[it.stateIndex - 1] }
        )
    }

    @Test
    fun `brackets that are not a task, and tasks in code, are left alone`() {
        val text = "[ ] Not a list item\n- [link](url)\n- [ ]no space\n```\n- [ ] In code\n```\n- [ ] Real\n"

        assertEquals(
            expected = listOf(text.indexOf(string = "- [ ] Real") + 3),
            actual = finder.tasks(text = text).map { it.stateIndex }
        )
    }

    @Test
    fun `the task on the cursor line toggles to the other state`() {
        val text = "Intro\n- [ ] Buy flour\n- [x] Soak beans"

        assertEquals(
            expected = listOf(null, 'x', ' '),
            actual = listOf(2, text.indexOf(string = "flour"), text.length).map { offset ->
                finder.taskAt(text = text, offset = offset)?.toggledState
            }
        )
    }
}
