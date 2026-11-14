package app.notekea.chat.domain.tool.reading

/**
 * A part of a text under one heading, its subheadings with it: the [heading] as it reads, its [level] (1 for `#`),
 * and where it [start]s and [end]s in the text. The part before the first heading has level 0 and no heading.
 */
data class OutlinePart(
    val heading: String,
    val level: Int,
    val start: Int,
    val end: Int
) {
    fun textIn(text: String): String = text
        .substring(
            startIndex = start,
            endIndex = end
        )
        .trim()
}
