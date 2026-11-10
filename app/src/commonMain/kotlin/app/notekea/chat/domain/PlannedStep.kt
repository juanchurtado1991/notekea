package app.notekea.chat.domain

/**
 * One step of the plan the model writes for a request (3.9): its [kind] and the [fields] it filled, as written, before
 * the app checks them against the vault. [notes] names several notes when a step covers them (up to 50).
 */
data class PlannedStep(
    val kind: ActionKind,
    val fields: Map<ActionField, String> = emptyMap(),
    val notes: List<String> = emptyList()
) {
    operator fun get(field: ActionField): String? = fields[field]?.trim()?.takeIf {
        it.isNotEmpty()
    }

    /** The fields [kind] needs that the model left empty. */
    val missing: Set<ActionField>
        get() = kind
            .needs
            .filterTo(destination = mutableSetOf()) {
                get(field = it) == null
            }
}
