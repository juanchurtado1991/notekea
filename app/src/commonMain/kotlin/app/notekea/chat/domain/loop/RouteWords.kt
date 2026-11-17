package app.notekea.chat.domain.loop

/**
 * What a request's own words say of its kind of work, where the router read it as a question: "busca las recetas
 * venezolanas y etiquétalas con latina" and "compare the two supplier meetings in a note …" were both routed to ask,
 * whose tools change nothing (the loop's seventh run, 2026-11-17).
 */
internal object RouteWords {
    /** [routed], or the work [task] names in so many words when it was routed to [WorkKind.ASK]. */
    fun kindOf(
        task: String,
        routed: WorkKind
    ): WorkKind {
        val words = task.lowercase()
        val isRead = routed == WorkKind.ASK && !QUESTION.containsMatchIn(input = words)

        return when {
            !isRead -> routed
            IN_A_NOTE.containsMatchIn(input = words) -> WorkKind.WRITE
            TAGS_WITH.containsMatchIn(input = words) -> WorkKind.ORGANIZE
            else -> routed
        }
    }

    private val IN_A_NOTE = Regex(pattern = "\\b(in|into|en) (a|una) (new |nueva )?(note|nota)\\b")

    private val TAGS_WITH = Regex(
        pattern = "\\b(tag|etiqueta|etiquetar|etiquétalas|etiquetalas|etiquétalos|etiquetalos)\\b.*\\b(with|con)\\b"
    )

    // "which notes did I tag with…", "¿qué escribí en una nota…?": a question changes nothing.
    private val QUESTION = Regex(
        pattern = "^\\W*(what|which|how|who|when|where|why|is|are|does|do|did|qué|que|cuál|cual|cuáles|cuales|cómo|" +
            "como|cuánto|cuánta|cuántos|cuántas|quién|dónde|cuándo)\\b"
    )
}
