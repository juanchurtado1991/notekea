package app.notekea.chat.domain.tool.reading

import app.notekea.core.notes.domain.OutlineFinder

/**
 * A text as the parts its headings make, so a long note or document is read a part at a time: each part runs from
 * its heading to the next heading of its level or above.
 */
internal object TextOutline {
    private val finder = OutlineFinder()
    private val WORDS = Regex(pattern = "[\\p{L}\\p{N}]+(['’-][\\p{L}\\p{N}]+)*")

    fun of(text: String): List<OutlinePart> {
        val headings = finder.headings(text = text)

        val parts = headings.mapIndexed { index, heading ->
            val next = headings
                .drop(n = index + 1)
                .firstOrNull {
                    it.level <= heading.level
                }

            OutlinePart(
                heading = heading.text,
                level = heading.level,
                start = heading.offset,
                end = next?.offset ?: text.length
            )
        }

        val before = OutlinePart(
            heading = "",
            level = 0,
            start = 0,
            end = headings.firstOrNull()?.offset ?: text.length
        )

        return listOf(before).filter { it.textIn(text = text).isNotBlank() } + parts
    }

    /** The part [section] names: its heading as written, else the first heading that holds those words. */
    fun find(
        parts: List<OutlinePart>,
        section: String
    ): OutlinePart? {
        val wanted = section
            .trim()
            .trimStart('#', ' ')
            .lowercase()

        val named = parts.filter {
            it.level > 0
        }

        return named.firstOrNull {
            it.heading.lowercase() == wanted
        }
            ?: named.firstOrNull {
                wanted in it.heading.lowercase()
            }
    }

    /** Counted here, not by the model: asked how many words a note has, it said 109, 128 and 63 (the chat probe). */
    fun wordsIn(text: String): Int = WORDS.findAll(input = text).count()
}
