package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * A clip as the note it becomes: `Web sources/<title>.md`, with the properties `source` and `clipped`, the notes, and
 * the article under `## Original text`, so search and the chat keep it if the page goes away.
 */
object WebClipNote {
    val FOLDER = RelativePath(value = "Web sources")
    private const val MAX_NAME = 80
    private val UNSAFE = Regex(pattern = "[\\\\/:*?\"<>|#^\\[\\]]")
    private const val ORIGINAL = "## Original text"

    /** The note's path in [FOLDER], from the clip's title, made safe for a file name. */
    fun pathOf(title: String): RelativePath {
        val name = title
            .replace(UNSAFE, " ")
            .replace(Regex(pattern = "\\s+"), " ")
            .trim()
            .take(n = MAX_NAME)
            .ifEmpty { "Web page" }

        return FOLDER.child(name = "$name${RelativePath.MARKDOWN_EXTENSION}")
    }

    /** The note's text; [today] is the date it was clipped (YYYY-MM-DD). */
    fun textOf(
        clip: WebClip,
        today: String
    ): String = buildString {
        append("---\nsource: \"").append(clip.url.replace("\"", "%22")).append("\"\n")
        append("clipped: ").append(today).append("\n---\n\n")
        append("# ").append(clip.title).append("\n\n")
        if (clip.notes.isNotBlank()) append(clip.notes.trim()).append("\n\n")
        append(ORIGINAL).append("\n\n")

        append(
            clip
                .article
                .text
                .trim()
        ).append('\n')
    }
}
