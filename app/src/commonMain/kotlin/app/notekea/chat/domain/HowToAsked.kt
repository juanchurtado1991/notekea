package app.notekea.chat.domain

/**
 * A request that asks how to do something is explained, never done, even when the model plans the action: "¿cómo
 * abro la nota de hoy?" planned as open_today, "como creo la nota?" as create_note (the developer's smoke, 2026-11-11).
 * "How many" or "how long" are questions about the notes, not about the app.
 */
internal object HowToAsked {
    private val HOW_TO = Regex(
        pattern = "^\\W*(how (do|can|should|would) (i|we|you)\\b|how to\\b|is there a (shortcut|way)\\b|" +
            "c[oó]mo (se|puedo|hago|creo|abro|cambio|busco|agrego|añado|borro|muevo|renombro|veo|pongo|uso|activo|" +
            "encuentro|enlazo|marco|etiqueto)\\b|hay (un atajo|una forma|alguna forma)\\b)",
        option = RegexOption.IGNORE_CASE
    )

    fun shaped(
        steps: List<PlannedStep>,
        request: String
    ): List<PlannedStep> {
        val isAsked = HOW_TO.containsMatchIn(input = request.trim())

        // Done, or talked about: either way a how-to is explained from the guide.
        val isHelped = steps.any {
            it.kind == ActionKind.HELP
        }

        val isNotHelp = steps.any { it.kind != ActionKind.ANSWER } && !isHelped

        if (!isAsked || !isNotHelp) return steps

        return listOf(
            PlannedStep(
                kind = ActionKind.HELP,
                fields = mapOf(ActionField.REQUEST to request)
            )
        )
    }
}
