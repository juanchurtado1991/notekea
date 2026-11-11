package app.notekea.chat.domain

/**
 * The period of a plan's tasks as the request says it: a period it does not name is dropped ("what tasks are open?"
 * read as "no date" hides every dated task), and one it names is filled in when the plan left it out or put it
 * under another field ("muéstrame las tareas de hoy" -> show_tasks property=today).
 */
internal object TaskPeriods {
    // In the order they are looked for: "no date" before "today", "later" holds no word of "overdue".
    private val WORDS = mapOf(
        "overdue" to listOf("overdue", "past due", "vencid", "atrasad"),
        "no date" to listOf("no date", "undated", "without a date", "sin fecha"),
        "today" to listOf("today", "hoy"),
        "this week" to listOf("this week", "esta semana"),
        "later" to listOf("later", "upcoming", "después", "despues", "más adelante", "próxim", "proxim"),
        "done" to listOf("done", "finish", "complet", "checked", "hech", "terminad", "acabad", "cerrad")
    )

    fun asked(
        step: PlannedStep,
        request: String
    ): PlannedStep {
        if (step.kind != ActionKind.SHOW_TASKS) return step

        val asked = request.lowercase()

        val said = WORDS
            .entries
            .firstOrNull { (_, words) ->
                words.any {
                    it in asked
                }
            }
            ?.key

        val period = step[ActionField.PERIOD]?.lowercase()
        val isNamed = period != null && WORDS[period]?.any { it in asked } != false
        if (isNamed) return step

        val fields = step.fields - ActionField.PERIOD

        return step.copy(
            fields = said?.let {
                fields + (ActionField.PERIOD to it)
            } ?: fields
        )
    }
}
