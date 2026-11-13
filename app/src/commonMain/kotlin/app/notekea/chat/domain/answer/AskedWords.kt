package app.notekea.chat.domain.answer

/** How a request is worded, for the plan's shaping rules: a question about the notes, or asking for writing. */
internal object AskedWords {
    private val WRITING = Regex(
        pattern = "\\b(note|notes|nota|notas|write|writes|written|escrib\\w*|create|cre[aá]\\w*|make a|make me|" +
            "haz|hac[eé]|hazme|save|guard\\w*|draft|redact\\w*|new|nuev[oa]|file|archivo|document\\w*|page|p[aá]gina|" +
            "table|tabla|list|lista)\\b",
        option = RegexOption.IGNORE_CASE
    )

    // A question about what the notes say, not a polite request ("can you open…") nor a how-to.
    private val ABOUT = Regex(
        pattern = "^\\W*(what|which|who|whom|whose|when|where|why|how (much|many|long|often)|" +
            "qu[eé]|cu[aá]l|cu[aá]les|qui[eé]n|qui[eé]nes|cu[aá]ndo|d[oó]nde|por qu[eé]|cu[aá]nt[oa]s?|" +
            "tell me|dime|decime|quiero (saber|que me digas)|is|are|does|do|did|est[aá]|hay)\\b",
        option = RegexOption.IGNORE_CASE
    )

    fun asksAbout(request: String): Boolean = ABOUT.containsMatchIn(input = request.trim())

    fun asksForWriting(request: String): Boolean = WRITING.containsMatchIn(input = request)
}
