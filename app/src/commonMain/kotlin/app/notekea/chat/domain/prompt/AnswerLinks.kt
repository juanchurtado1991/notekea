package app.notekea.chat.domain.prompt

import app.notekea.core.web.domain.WebHost

/**
 * The links of an answer, kept only when they lead where the turn has been: the 4B ended nearly every answer from the
 * notes with a link it made up, "[site](https://example.com/greenhouse)" (the loop's fifth run, 2026-11-16).
 */
internal object AnswerLinks {
    /** A web address, as a task, a search or a page read writes it. */
    val ADDRESS = Regex(pattern = "https?://[^\\s)\\]>\"']+")

    /**
     * [answer] without the links to a site [known] (the task and what the turn found) never names: its words stay
     * when they say something ("Budget 2026"), and go with it when they do not ("site", "read …").
     */
    fun kept(
        answer: String,
        known: String
    ): String {
        val sites = ADDRESS
            .findAll(input = known)
            .map { WebHost.of(address = it.value) }
            .toSet()

        return LINK
            .replace(input = answer) { link ->
                val words = link.groupValues[WORDS].trim()
                val isKnown = WebHost.of(address = link.groupValues[WHERE]) in sites

                val isEmpty =
                    words.lowercase() in EMPTY_WORDS || READ.containsMatchIn(input = words)

                when {
                    isKnown -> link.value
                    isEmpty -> ""
                    else -> link.groupValues[BEFORE] + words
                }
            }
            .replace(
                regex = LEFT_OPEN,
                replacement = ""
            )
            .replace(
                regex = BEFORE_STOP,
                replacement = "$1"
            )
            .trim()
    }

    private const val BEFORE = 1
    private const val WORDS = 2
    private const val WHERE = 3

    private val LINK = Regex(pattern = "([ \\t]*)\\[([^\\]]*)]\\((https?://[^)\\s]*)\\)")

    // "()" and "[]" a link left behind, and the space before a full stop.
    private val LEFT_OPEN = Regex(pattern = "[ \\t]*(\\(\\s*\\)|\\[\\s*])")
    private val BEFORE_STOP = Regex(pattern = "[ \\t]+([.,;:])")

    private val READ = Regex(
        pattern = "^(read|search|find|list|leer|buscar)\\b",
        option = RegexOption.IGNORE_CASE
    )

    private val EMPTY_WORDS = setOf(
        "site", "link", "source", "page", "note", "here", "sitio", "enlace", "fuente", "página", "pagina", "nota",
        "aquí", "aqui"
    )
}
