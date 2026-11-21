package app.notekea.chat.domain.plan

/**
 * A note's title as its file's name: the characters no system allows in one are left out. One rule for the plan's
 * checks and for the writer, so a later step finds a new note where it is written ("Ideas?" is `Ideas.md`).
 */
object NoteFileName {
    private const val UNTITLED = "Untitled"
    private val FORBIDDEN = Regex(pattern = "[\\\\/:*?\"<>|#^\\[\\]]")

    fun of(title: String?): String = title
        .orEmpty()
        .replace(FORBIDDEN, "")
        .trim()
        .ifEmpty { UNTITLED }
}
