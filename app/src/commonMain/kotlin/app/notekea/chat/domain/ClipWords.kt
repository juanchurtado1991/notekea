package app.notekea.chat.domain

/**
 * What a request to clip a page says of the pages it links: only the page ("clip only …", "solo esta página") or them
 * too ("with its linked pages", "con sus enlaces"); neither, and the chat asks.
 */
internal object ClipWords {
    private val ONLY = Regex(
        pattern = "\\b(only|just|solo|sólo|solamente|únicamente)\\b",
        option = RegexOption.IGNORE_CASE
    )

    private val SECTION = Regex(
        pattern = "(linked pages|its links|with (its|the) links|whole section|all (its|the) pages|internal links|" +
            "enlaces|p[aá]ginas enlazadas|toda la secci[oó]n|todas las p[aá]ginas|v[ií]nculos|hiperv[ií]nculos)",
        option = RegexOption.IGNORE_CASE
    )

    fun isOnly(request: String): Boolean =
        ONLY.containsMatchIn(input = request) && !isSection(request)

    fun isSection(request: String): Boolean = SECTION.containsMatchIn(input = request)
}
