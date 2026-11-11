package app.notekea.chat.domain

/**
 * The property a request gives the note it creates, when the plan left it out: "create a note Call with Ana in
 * Projects, status draft" ends in a property the vault has and its value, which a small model may not plan.
 */
internal object PropertiesNamed {
    // The last part of the request: ", status draft" or ", status: draft".
    private val TRAILING = Regex(pattern = ",\\s*([\\p{L}_-]+)\\s*:?\\s+([^,\\s]+)\\s*[.!]?\\s*$")

    fun added(
        steps: List<PlannedStep>,
        request: String,
        context: ActionContext
    ): List<PlannedStep> {
        val created = steps.singleOrNull {
            it.kind == ActionKind.CREATE_NOTE
        }

        val title = created?.get(ActionField.TITLE) ?: return steps

        val (key, value) = TRAILING
            .find(input = request)
            ?.destructured
            ?: return steps

        val property = context.properties.firstOrNull { it.equals(key, ignoreCase = true) }

        val isSet = steps.any {
            it.kind == ActionKind.SET_PROPERTY
        }

        val isMissing =
            property != null &&
                !isSet &&
                steps.size < ActionPrompt.MAX_STEPS

        if (!isMissing) return steps

        return steps + PlannedStep(
            kind = ActionKind.SET_PROPERTY,
            fields = mapOf(
                ActionField.NOTE to title,
                ActionField.PROPERTY to property.orEmpty(),
                ActionField.VALUE to value
            )
        )
    }
}
