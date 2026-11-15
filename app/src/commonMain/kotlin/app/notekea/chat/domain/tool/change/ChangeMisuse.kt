package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.PlannedStep

/**
 * A change tool called for what another one does, read from the task's words (`docs/agent-loop.md`, "Why the loop
 * fails"): what to call instead, told once, or null when the call fits the task.
 */
internal object ChangeMisuse {
    /** create_note for a note the task says what to hold: it would be made empty. */
    fun emptyNote(
        task: String,
        step: PlannedStep
    ): String? {
        val isFilled =
            HOLDS.containsMatchIn(input = task.lowercase()) && step[ActionField.TEMPLATE] == null

        return if (isFilled) {
            "create_note makes an empty note, and the task says what the note holds. Read the notes it is " +
                "written from, then call write_note with them; create_note only for a note left empty."
        } else {
            null
        }
    }

    /** append for a task the task asks to add: a line of text is no task. */
    fun taskAppended(
        task: String,
        step: PlannedStep
    ): String? =
        if (ADDS_TASK.containsMatchIn(input = task.lowercase()) && step[ActionField.TEXT] != null) {
            "append adds text, and the task asks for a task: call add_task with the note and the task's text."
        } else {
            null
        }

    /** complete_task where nothing is asked to be done: a status set or a task added. */
    fun doneUnasked(
        task: String,
        step: PlannedStep
    ): String? {
        val words = task.lowercase()

        return when {
            STATUS.containsMatchIn(input = words) ->
                "complete_task marks a task done, and the task asks for a property: call set_property with " +
                    "the note, the property and its value."

            !DONE.containsMatchIn(input = words) ->
                "complete_task marks a task done, and the task does not ask that: add_task adds a task, " +
                    "set_property sets a property."

            else -> null
        }.takeIf {
            step[ActionField.NOTE] != null
        }
    }

    // "a note X with…", "listing…", "Lisbon plan: the places…", "con las recetas…".
    private val HOLDS = Regex(pattern = "\\b(with|listing|holding|con|listando|que tenga)\\b|\\w: ")
    private val ADDS_TASK =
        Regex(pattern = "\\b(add|put|añade|agrega|pon)\\b.*\\b(task|tarea|to-?do)\\b")
    private val STATUS =
        Regex(pattern = "\\b(status|estado|property|propiedad|priority|prioridad)\\b")
    private val DONE = Regex(
        pattern = "\\b(done|complete|completed|finish|finished|check off|tick|hecha|hecho|completa|termina|" +
            "terminada|marca)\\b"
    )
}
