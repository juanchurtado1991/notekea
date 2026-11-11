package app.notekea.chat.domain

/**
 * "Cambiá el tema a oscuro": a set_theme step the model left without a theme gets the app's dark or light mode from
 * the request's words (the chat probe, 2026-11-11).
 */
internal object ThemeWords {
    private val MODES = mapOf(
        "dark" to listOf("dark", "oscuro", "oscura", "noche", "night"),
        "light" to listOf("light", "claro", "clara", "día", "dia")
    )

    fun shaped(
        step: PlannedStep,
        request: String
    ): PlannedStep {
        val isMissing = step.kind == ActionKind.SET_THEME && step[ActionField.THEME] == null
        if (!isMissing) return step

        val words = request.lowercase()

        val mode = MODES
            .entries
            .firstOrNull { (_, said) ->
                said.any {
                    it in words
                }
            }
            ?.key ?: return step

        return step.copy(fields = step.fields + (ActionField.THEME to mode))
    }
}
