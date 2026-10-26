package app.notekea.notes.domain

/**
 * The `#tags` written in a note's text, as Obsidian reads them, each once in the order they first appear: a `#` not
 * after a letter, digit, `#`, `/` or `&`, followed by letters, digits, `_`, `-` and `/`, not only digits. Code (fenced
 * blocks and inline code), wiki links and link targets are not text, so `[[#Heading]]` and `(#anchor)` are not tags.
 */
class TextTagFinder {
    fun tags(
        lines: Sequence<String>
    ): List<String> {
        val tags = LinkedHashSet<String>()
        var fence: Char? = null
        lines.forEach { line ->
            val marker = FENCE.find(input = line)?.groupValues?.get(index = 1)?.first()
            when {
                fence != null -> if (marker == fence) fence = null

                marker != null -> fence = marker

                else -> {
                    val text = line.replace(regex = NOT_TEXT, replacement = " ")
                    TAG.findAll(input = text).forEach { tags += it.groupValues[1] }
                }
            }
        }
        return tags.toList()
    }

    companion object {
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
        private val NOT_TEXT = Regex(pattern = "(`+).*?\\1|\\[\\[[^\\]\\n]*]]|]\\([^)\\n]*\\)")
        private val TAG = Regex(pattern = "(?<![\\w#/&])#([\\p{L}\\p{N}_/-]*[\\p{L}_/-][\\p{L}\\p{N}_/-]*)")
    }
}
