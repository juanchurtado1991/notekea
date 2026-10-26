package app.notekea.notes.domain

/**
 * One line of Markdown as a reader sees it, for search results: the block marker at the start (heading `#`, quote `>`,
 * list `-` or `1.`, task box), the brackets of wiki links (an alias replaces its target), the address of Markdown
 * links and images (their text stays), inline code backticks and emphasis marks (`**`, `__`, `~~`, `==`, `*`, `_`)
 * are removed; `#tags` and everything else stay. Inside inline code nothing but the backticks is touched. Highlight
 * ranges move with the text they cover; a range whose characters were all removed is dropped.
 */
class MarkdownLineStripper {
    fun strip(
        line: String,
        ranges: List<IntRange>
    ): PlainLine {
        val removed = BooleanArray(size = line.length)
        val code = BooleanArray(size = line.length)
        removeLeadingMarker(line = line, removed = removed)
        INLINE_CODE.findAll(input = line).forEach { match ->
            val ticks = match.groupValues[1].length
            match.range.forEach { code[it] = true }
            remove(removed = removed, range = match.range.first until match.range.first + ticks)
            remove(removed = removed, range = match.range.last - ticks + 1..match.range.last)
        }
        removeLinkSyntax(line = line, removed = removed, code = code)
        listOf(PAIRED_EMPHASIS, SINGLE_EMPHASIS).forEach { pattern ->
            pattern.findAll(input = line).filter { !code[it.range.first] }.forEach { match ->
                val marks = match.groupValues[1].length
                remove(removed = removed, range = match.range.first until match.range.first + marks)
                remove(removed = removed, range = match.range.last - marks + 1..match.range.last)
            }
        }
        return plainLine(line = line, removed = removed, ranges = ranges)
    }

    private fun removeLeadingMarker(
        line: String,
        removed: BooleanArray
    ) {
        LEADING_MARKER.find(input = line)?.range?.let { remove(removed = removed, range = it) }
    }

    /** `[[target|alias]]` keeps the alias, `[[target]]` the target, `[text](address)` the text; embeds the same. */
    private fun removeLinkSyntax(
        line: String,
        removed: BooleanArray,
        code: BooleanArray
    ) {
        WIKI_LINK.findAll(input = line).filter { !code[it.range.first] }.forEach { match ->
            val alias = match.groups[ALIAS_GROUP]
            val textStart = alias?.range?.first ?: match.groups[TARGET_GROUP]!!.range.first
            remove(removed = removed, range = match.range.first until textStart)
            remove(removed = removed, range = match.range.last - 1..match.range.last)
        }
        MARKDOWN_LINK.findAll(input = line).filter { !code[it.range.first] }.forEach { match ->
            val text = match.groups[TEXT_GROUP]!!.range
            remove(removed = removed, range = match.range.first until text.first)
            remove(removed = removed, range = text.last + 1..match.range.last)
        }
    }

    private fun remove(
        removed: BooleanArray,
        range: IntRange
    ) {
        range.forEach { if (it in removed.indices) removed[it] = true }
    }

    /** The kept characters, and each range moved to them (from its first kept character to its last). */
    private fun plainLine(
        line: String,
        removed: BooleanArray,
        ranges: List<IntRange>
    ): PlainLine {
        val newIndex = IntArray(size = line.length)
        val text = buildString {
            line.forEachIndexed { index, char ->
                newIndex[index] = length
                if (!removed[index]) append(char)
            }
        }
        val moved = ranges.mapNotNull { range ->
            val kept = range.filter { it in line.indices && !removed[it] }
            if (kept.isEmpty()) null else newIndex[kept.first()]..newIndex[kept.last()]
        }
        return PlainLine(text = text.trimEnd(), ranges = moved)
    }

    companion object {
        private const val ALIAS_GROUP = 2
        private const val TARGET_GROUP = 1
        private const val TEXT_GROUP = 1
        private val LEADING_MARKER = Regex(
            pattern = "^\\s*(?:>\\s*)*(?:#{1,6}\\s+|(?:[-*+]|\\d+[.)])\\s+(?:\\[[ xX]]\\s+)?)?"
        )
        private val INLINE_CODE = Regex(pattern = "(`+)(.+?)\\1")
        private val WIKI_LINK = Regex(pattern = "!?\\[\\[([^\\]|\\n]+)(?:\\|([^\\]\\n]*))?]]")
        private val MARKDOWN_LINK = Regex(pattern = "!?\\[([^\\]\\n]*)]\\([^)\\n]*\\)")
        private val PAIRED_EMPHASIS = Regex(pattern = "(\\*\\*|__|~~|==)(?=\\S)(.+?)(?<=\\S)\\1")
        private val SINGLE_EMPHASIS =
            Regex(pattern = "(?<![\\w*\\\\])([*_])(?=[^\\s*_])(.+?)(?<=[^\\s*_])\\1(?![\\w*])")
    }
}
