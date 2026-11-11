package app.notekea.chat.domain

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** What [PlanRequestUseCase] corrects in a small model's plan, from what the request itself says. */
class PlanShapingTest {
    private val engine = FakeLlmEngine()

    private suspend fun planned(
        request: String,
        plan: String
    ): List<PlannedStep> {
        engine.reply = { plan }

        val result = PlanRequestUseCase(engine, GhostActionPlanReader())(
            request = request,
            context = ActionContext(
                today = "2026-11-10",
                folders = listOf("Meetings", "Projects", "Projects/Garden"),
                properties = listOf("rating", "status"),
                notes = listOf("Templates/Meeting", "Meetings/Retro")
            )
        ) as Outcome.Success

        return result.value
    }

    @Test
    fun `a period of tasks is kept only when the request names it`() = runTest {
        val open = planned(
            request = "what tasks are open?",
            plan = "{\"steps\":[{\"action\":\"show_tasks\",\"period\":\"no date\"}]}"
        )

        val week = planned(
            request = "¿qué tareas hay esta semana?",
            plan = "{\"steps\":[{\"action\":\"show_tasks\",\"period\":\"this week\"}]}"
        )

        val finished = planned(
            request = "which tasks did I finish?",
            plan = "{\"steps\":[{\"action\":\"show_tasks\",\"period\":\"done\"}]}"
        )

        val today = planned(
            request = "muéstrame las tareas de hoy",
            plan = "{\"steps\":[{\"action\":\"show_tasks\",\"property\":\"today\"}]}"
        )

        assertEquals(
            expected = listOf(null, "this week", "done", "today"),
            actual = (open + week + finished + today).map {
                it[ActionField.PERIOD]
            }
        )
    }

    @Test
    fun `a template the request names is kept when the plan lists it among the notes`() = runTest {
        val steps = planned(
            request = "create a note \"Meeting with Ana\" in Meetings from the Meeting template",
            plan = "{\"steps\":[{\"action\":\"create_note\",\"title\":\"Meeting with Ana\"," +
                "\"notes\":[\"Meetings\",\"Templates/Meeting\"]}]}"
        )

        assertEquals(
            expected = "Meeting",
            actual = steps.single()[ActionField.TEMPLATE]
        )
    }

    @Test
    fun `a word of the title is not taken for what the new note holds`() = runTest {
        val steps = planned(
            request = "create a note Call with Ana in Projects, status draft",
            plan = "{\"steps\":[{\"action\":\"create_note\",\"title\":\"Call with Ana\",\"notes\":[\"Call with Ana\"]}," +
                "{\"action\":\"set_property\",\"note\":\"Call with Ana\",\"property\":\"status\",\"value\":\"draft\"}]}"
        )

        assertEquals(
            expected = listOf(null, "Projects", ActionKind.SET_PROPERTY),
            actual = listOf(steps[0][ActionField.TEXT], steps[0][ActionField.FOLDER], steps[1].kind)
        )
    }

    @Test
    fun `a property the request gives the new note is set even when the plan leaves it out`() =
        runTest {
            val left = planned(
                request = "create a note Budget in Projects, status: todo",
                plan = "{\"steps\":[{\"action\":\"create_note\",\"title\":\"Budget\",\"notes\":[\"Projects\"]}]}"
            )

            val unknown = planned(
                request = "create a note Budget in Projects, color blue",
                plan = "{\"steps\":[{\"action\":\"create_note\",\"title\":\"Budget\",\"notes\":[\"Projects\"]}]}"
            )

            assertEquals(
                expected = listOf(
                    mapOf(
                        ActionField.NOTE to "Budget",
                        ActionField.PROPERTY to "status",
                        ActionField.VALUE to "todo"
                    ),
                    null
                ),
                actual = listOf(left, unknown).map { it.getOrNull(index = 1)?.fields }
            )
        }

    @Test
    fun `a folder that holds only folders is one the plan may name`() {
        val folders = ActionContext.foldersOf(
            paths = listOf(
                RelativePath(value = "Projects/Garden/Tomato plan.md"),
                RelativePath(value = "Home.md")
            )
        )

        assertEquals(
            expected = listOf("Projects", "Projects/Garden"),
            actual = folders
        )
    }

    @Test
    fun `asking how to do something is explained even when the model plans the action`() = runTest {
        val today = planned(
            request = "¿cómo abro la nota de hoy?",
            plan = "{\"steps\":[{\"action\":\"open_today\"}]}"
        )

        val note = planned(
            request = "como creo la nota?",
            plan = "{\"steps\":[{\"action\":\"create_note\",\"title\":\"La nota\"}]}"
        )

        val recipe = planned(
            request = "how do I make arepas?",
            plan = "{\"steps\":[{\"action\":\"answer\"}]}"
        )

        assertEquals(
            expected = listOf(ActionKind.HELP, ActionKind.HELP, ActionKind.ANSWER),
            actual = (today + note + recipe).map { it.kind }
        )
    }
}
