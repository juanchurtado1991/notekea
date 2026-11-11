package app.notekea.chat.domain

/**
 * The template a request names for its new note, when the plan lost it: "create a note Meeting with Ana in Meetings
 * from the Meeting template", where a small model lists the template among the step's notes with the folder.
 */
internal object TemplateNamed {
    private val SAID = listOf(
        Regex(
            pattern = "from the (.+?) template",
            option = RegexOption.IGNORE_CASE
        ),
        Regex(
            pattern = "(?:con|desde) la plantilla (?:de )?([^,.]+)",
            option = RegexOption.IGNORE_CASE
        )
    )

    fun asked(
        step: PlannedStep,
        request: String,
        context: ActionContext
    ): PlannedStep {
        val isLost = step.kind == ActionKind.CREATE_NOTE && step[ActionField.TEMPLATE] == null
        if (!isLost) return step

        val name = SAID.firstNotNullOfOrNull {
            it
                .find(input = request)
                ?.groupValues
                ?.get(index = 1)
                ?.trim()
        } ?: return step

        // Only a template the vault has: a note of that name in a folder of templates.
        val template = context
            .notes
            .firstOrNull { note ->
                val folder = note.substringBeforeLast(delimiter = '/')
                val title = note.substringAfterLast(delimiter = '/')

                val isTemplates = folder.contains(TEMPLATES, ignoreCase = true)

                isTemplates && title.equals(name, ignoreCase = true)
            }
            ?.substringAfterLast(delimiter = '/')
            ?: return step

        return step.copy(fields = step.fields + (ActionField.TEMPLATE to template))
    }

    private const val TEMPLATES = "template"
}
