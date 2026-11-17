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

    /**
     * set_property with the value a task tags with: "tag the notes that have a status property with tracked" had
     * both models set status to tracked on every project (the loop's fifth run).
     */
    fun tagAsProperty(
        task: String,
        step: PlannedStep
    ): String? {
        val words = task.lowercase()
        val value = step[ActionField.VALUE]?.lowercase()?.removePrefix(prefix = "#") ?: return null

        val isTagged = TAGS.containsMatchIn(input = words) && Regex.escape(value).let {
            Regex(pattern = "\\b(with|con) #?$it\\b").containsMatchIn(input = words)
        }

        return if (isTagged) {
            "set_property sets a property, and the task tags with \"$value\": call add_tag with the notes and " +
                "that tag."
        } else {
            null
        }
    }

    /**
     * add_task where the task names another change and no task: "tag the notes about my home renovation" got "Task to
     * tag home renovation notes" added to one of them, and four more like it (the loop's seventh run). Every time.
     */
    fun taskUnasked(
        task: String,
        step: PlannedStep
    ): String? {
        val words = task.lowercase()

        val isUnasked =
            OTHER_CHANGE.containsMatchIn(input = words) && !ASKS_TASK.containsMatchIn(input = words)

        return if (isUnasked && step[ActionField.TEXT] != null) {
            "The task asks for no task, and nothing it does not ask for is added. When the proposal holds " +
                "what the task asks, call propose now."
        } else {
            null
        }
    }

    /** A tag the task does not write, where it writes the tag itself (with X, con X): "travel" for "con viaje". */
    fun isTagNamed(task: String): Boolean {
        val words = task.lowercase()

        return TAGS.containsMatchIn(input = words) && TAG_NAMED.containsMatchIn(input = words)
    }

    private val TAG_NAMED = Regex(pattern = "\\b(with|con)\\s+#?[\\p{L}\\p{N}_/-]+")
    private val ASKS_TASK = Regex(
        pattern = "\\b(task|tasks|to-?do|to-?dos|remind|reminder|tarea|tareas|pendiente|pendientes|recuérdame|" +
            "recuerdame|recordatorio)\\b"
    )
    private val OTHER_CHANGE = Regex(
        pattern = "\\b(tag|tags|etiqueta|etiquetas|etiquetar|etiquétalas|etiquetalas|etiquétalos|move|mueve|mover|" +
            "rename|renombra|renombrar|set|property|propiedad|link|enlaza|enlazar)\\b"
    )

    private val TAGS =
        Regex(pattern = "\\b(tag|tags|etiqueta|etiquetas|etiquétalas|etiquetalas|etiquetar)\\b")

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
