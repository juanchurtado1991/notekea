package app.notekea.chat.domain.plan

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

    fun isSection(request: String): Boolean =
        SECTION.containsMatchIn(input = request) ||
            DEPTH.containsMatchIn(input = request) ||
            isFull(request)

    /** "… the whole documentation", "documentación completa": every level of links, every page. */
    fun isFull(request: String): Boolean = FULL.containsMatchIn(input = request)

    /** "…, count first": the pages are counted and the person asked again before any is clipped. */
    fun isCounted(request: String): Boolean = COUNT_FIRST in request

    // "with all its linked pages", "con todas sus páginas": every page, not only the closest 30.
    fun isEvery(request: String): Boolean =
        isSection(request) && EVERY.containsMatchIn(input = request)

    /** "… 2 levels deep", "a 3 niveles": how many levels of links are followed, 1 when the request says none. */
    fun depthOf(request: String): Int {
        if (isFull(request)) return FULL_DEPTH

        return DEPTH
            .find(input = request)
            ?.groupValues
            ?.get(index = 1)
            ?.toIntOrNull()
            ?.coerceIn(1, MAX_DEPTH)
            ?: 1
    }

    private const val MAX_DEPTH = 3

    /** As many levels as "the whole documentation" follows. */
    const val FULL_DEPTH = 8
    const val COUNT_FIRST = "count first"

    private val FULL = Regex(
        pattern = "(whole documentation|full documentation|full scan|complete documentation|" +
            "documentaci[oó]n completa|toda la documentaci[oó]n|escaneo completo)",
        option = RegexOption.IGNORE_CASE
    )

    private val DEPTH = Regex(
        pattern = "\\b(\\d)\\s*(levels?|niveles?)\\b",
        option = RegexOption.IGNORE_CASE
    )

    private val EVERY = Regex(
        pattern = "\\b(all|every|todas|todos|cada)\\b",
        option = RegexOption.IGNORE_CASE
    )
}
