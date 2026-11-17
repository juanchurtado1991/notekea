package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * set_property with the value a task tags with is pointed to add_tag: both models set status to tracked on every
 * project for "tag the notes that have a status property with tracked" (the loop's fifth run).
 */
class TagAsPropertyTest {
    private fun step(value: String) = PlannedStep(
        kind = ActionKind.SET_PROPERTY,
        fields = mapOf(
            ActionField.PROPERTY to "status",
            ActionField.VALUE to value
        ),
        notes = listOf("Projects/Garden plan")
    )

    @Test
    fun `a property set to the tag the task tags with is pointed to add_tag, and a status asked is not`() {
        assertEquals(
            expected = listOf(true, true, false),
            actual = listOf(
                "tag the notes in my Projects folder that have a status property with tracked" to "tracked",
                "etiqueta con pendiente los proyectos que tienen tareas abiertas" to "pendiente",
                "set status done on the project due on 15 November" to "done"
            ).map { (task, value) ->
                ChangeMisuse.tagAsProperty(
                    task = task,
                    step = step(value = value)
                ) != null
            }
        )
    }
}
