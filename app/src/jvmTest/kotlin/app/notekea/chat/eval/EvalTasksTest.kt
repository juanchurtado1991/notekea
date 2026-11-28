package app.notekea.chat.eval

import kotlin.test.Test
import kotlin.test.assertEquals

class EvalTasksTest {
    private val tasks =
        listOf("Mark the water bill as paid", "what is my budget?", "tag my sleep log")

    @Test
    fun `with neither words nor a list every task is held`() {
        val all = EvalTasks(
            words = emptyList(),
            listed = null
        )

        assertEquals(
            expected = tasks,
            actual = tasks.filter { all.holds(task = it) }
        )
    }

    @Test
    fun `words hold the tasks that write one of them`() {
        val worded = EvalTasks(
            words = listOf("budget", "sleep"),
            listed = null
        )

        assertEquals(
            expected = tasks.drop(n = 1),
            actual = tasks.filter { worded.holds(task = it) }
        )
    }

    @Test
    fun `a list holds its tasks as written, whatever their case, and no task that only holds one`() {
        val listed = EvalTasks(
            words = emptyList(),
            listed = setOf("mark the water bill as paid", "budget")
        )

        assertEquals(
            expected = tasks.take(n = 1),
            actual = tasks.filter { listed.holds(task = it) }
        )
    }

    @Test
    fun `words and a list together hold what both do`() {
        val both = EvalTasks(
            words = listOf("budget"),
            listed = setOf("mark the water bill as paid", "what is my budget?")
        )

        assertEquals(
            expected = listOf("what is my budget?"),
            actual = tasks.filter { both.holds(task = it) }
        )
    }
}
