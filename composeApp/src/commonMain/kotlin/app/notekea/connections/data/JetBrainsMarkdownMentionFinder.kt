package app.notekea.connections.data

import app.notekea.connections.domain.MentionFinder
import app.notekea.core.notes.data.MarkdownCodeRanges

/**
 * [MentionFinder] with patterns: each name as a whole word in any letter case (no letter, digit, `_` or `#` right
 * before it, no letter, digit or `_` right after), outside code ([MarkdownCodeRanges]), links, embeds and web
 * addresses. When names overlap ("Arepas" and an alias "Arepas reina"), the longer one at a place wins.
 */
class JetBrainsMarkdownMentionFinder : MentionFinder {
    private val codeRanges = MarkdownCodeRanges()

    override fun find(
        markdown: String,
        names: List<String>
    ): List<IntRange> {
        val found = names.filter {
            it.isNotBlank()
        }.flatMap { name -> wordsOf(name = name).findAll(input = markdown).map { it.range }.toList() }
        if (found.isEmpty()) return emptyList()
        val excluded = codeRanges.of(markdown = markdown) +
            EXCLUDED.flatMap { pattern -> pattern.findAll(input = markdown).map { it.range }.toList() }
        val kept = found.filterNot { place -> excluded.any { place.first in it || place.last in it } }
        return kept.sortedWith(compareBy<IntRange> { it.first }.thenByDescending { it.last })
            .fold(initial = emptyList()) { taken, place ->
                if (taken.lastOrNull()?.let { place.first <= it.last } ==
                    true
                ) {
                    taken
                } else {
                    taken + listOf(place)
                }
            }
    }

    private fun wordsOf(
        name: String
    ): Regex = Regex(
        pattern = "(?<![\\p{L}\\p{N}_#])" + Regex.escape(literal = name.trim()) + "(?![\\p{L}\\p{N}_])",
        option = RegexOption.IGNORE_CASE
    )

    private companion object {
        val EXCLUDED = listOf(
            // [[wiki links]] and ![[embeds]], with their headings, blocks and aliases.
            Regex(pattern = """!?\[\[[^\]\n]*]]"""),
            // [text](address) and ![image](address), whatever the address.
            Regex(pattern = """!?\[[^\]\n]*]\([^)\n]*\)"""),
            // Web addresses written as they are.
            Regex(pattern = """<?\b[a-zA-Z][a-zA-Z0-9+.-]*://[^\s>)]+>?""")
        )
    }
}
