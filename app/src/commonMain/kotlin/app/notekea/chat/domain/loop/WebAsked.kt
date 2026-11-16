package app.notekea.chat.domain.loop

import app.notekea.chat.domain.plan.UrlsNamed

/**
 * Whether a request asks for the web (`docs/agent-web.md`): it writes a page's address, or asks to research or look
 * something up online; and whether it asks for a note, so a note written from the web gets the web's tools.
 */
internal object WebAsked {
    private val RESEARCH = Regex(
        pattern = "\\b(research\\w*|investig\\w*|look (it |this |them )?up|online|on the web|on the internet|" +
            "search the web|en internet|en la web|busca en internet|with sources|con (las |sus )?fuentes|" +
            "cite (the )?sources|cita (las )?fuentes)\\b",
        option = RegexOption.IGNORE_CASE
    )

    // A note asked for: "write a note", "make detailed notes of", "add it to my trip note", "hazme una nota", "guarda
    // esta página como nota", "add them to my reading list".
    private val NOTE_ASKED = Regex(
        pattern = "\\b(make|write|take|save|keep|turn|add|put|haz|hazme|hacer|guarda|guardar|agrega|agregar|añade|" +
            "añadir|crea|crear|escribe|pon)\\b[^.?!\\n]{0,80}\\b(notes?|notas?|reading list|lista de lectura)\\b",
        option = RegexOption.IGNORE_CASE
    )

    /** Whether [task] asks for a note to be written or added to, not only for an answer. */
    fun isNoteAsked(task: String): Boolean = NOTE_ASKED.containsMatchIn(
        input = task.replace(
            regex = ADDRESS,
            replacement = "the page"
        )
    )

    // An address's dots would end the sentence the note is asked for in: "add what https://a.org/b says to my note".
    private val ADDRESS = Regex(pattern = "https?://\\S+")

    fun isIn(task: String): Boolean = pagesOf(task = task).isNotEmpty() || isResearch(task = task)

    /** Whether [task] asks to research or look something up on the web, whatever it then asks for. */
    fun isResearch(task: String): Boolean = RESEARCH.containsMatchIn(input = task)

    /** The pages [task] writes the address of. */
    fun pagesOf(task: String): List<String> = UrlsNamed.writtenIn(text = task)
}
