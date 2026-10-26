package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The embedded files of a note's text (`ROADMAP.md` 1.8), as Obsidian reads them: `![[file]]` with an optional
 * `|300` width or `|caption`, and `![alt](path)` (a path in `<…>` or percent-encoded, an optional `|300` after the
 * alt). Embeds of notes (`![[Other note]]`, a name without an extension or ending in `.md`) are links, not files, and
 * are left out; so is anything in code.
 */
class EmbedFinder {
    fun embeds(
        text: String
    ): List<Embed> {
        val embeds = mutableListOf<Embed>()
        var fence: Char? = null
        var lineStart = 0
        text.split(StringPatterns.NEW_LINE_CHAR).forEach { line ->
            val marker = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)?.first()
            when {
                fence != null -> if (marker == fence) fence = null
                marker != null -> fence = marker
                else -> embeds += embedsOf(line = line, offset = lineStart)
            }
            lineStart += line.length + 1
        }
        return embeds.filter { isFile(target = it.target) }
    }

    private fun embedsOf(
        line: String,
        offset: Int
    ): List<Embed> {
        val code = INLINE_CODE.findAll(input = line).map { it.range }.toList()
        val found = WIKI_EMBED.findAll(input = line).map { wikiEmbed(match = it, line = line, offset = offset) } +
            MARKDOWN_EMBED.findAll(input = line).map { markdownEmbed(match = it, line = line, offset = offset) }
        return found.filter { embed ->
            code.none { it.first <= embed.range.first - offset && embed.range.last - offset <= it.last }
        }.sortedBy { it.range.first }.toList()
    }

    private fun wikiEmbed(
        match: MatchResult,
        line: String,
        offset: Int
    ): Embed {
        val option = match.groupValues[2].takeIf { match.groups[2] != null }
        return Embed(
            range = match.range.first + offset..match.range.last + offset,
            target = match.groupValues[1].trim(),
            kind = EmbedKind.WIKI,
            isOwnLine = line.trim() == match.value,
            label = option?.takeIf { WIDTH.matchEntire(input = it) == null },
            width = option?.let { WIDTH.matchEntire(input = it)?.groupValues?.get(index = 1)?.toInt() }
        )
    }

    private fun markdownEmbed(
        match: MatchResult,
        line: String,
        offset: Int
    ): Embed {
        val alt = match.groupValues[1]
        val width = WIDTH_AFTER_ALT.find(input = alt)
        val written = match.groupValues[2].trim()
        val target = if (written.startsWith(prefix = ANGLE_OPEN) && written.endsWith(suffix = ANGLE_CLOSE)) {
            written.removePrefix(prefix = ANGLE_OPEN).removeSuffix(suffix = ANGLE_CLOSE)
        } else {
            percentDecoded(target = written)
        }
        return Embed(
            range = match.range.first + offset..match.range.last + offset,
            target = target,
            kind = EmbedKind.MARKDOWN,
            isOwnLine = line.trim() == match.value,
            label = (if (width != null) alt.removeRange(range = width.range) else alt).ifBlank { null },
            width = width?.groupValues?.get(index = 1)?.toInt()
        )
    }

    /** A file, not a note: its name has an extension other than `.md`. */
    private fun isFile(
        target: String
    ): Boolean {
        val name = target.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR)
            .substringAfterLast(delimiter = RelativePath.SEPARATOR)
        return EXTENSION.containsMatchIn(input = name) &&
            !name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION, ignoreCase = true)
    }

    companion object {
        private const val ANGLE_CLOSE = ">"
        private const val ANGLE_OPEN = "<"
        private val EXTENSION = Regex(pattern = "\\.[A-Za-z0-9]{1,8}$")
        private val INLINE_CODE = Regex(pattern = "(`+).+?\\1")
        private val MARKDOWN_EMBED = Regex(pattern = "!\\[([^\\]\\n]*)]\\(([^)\\n]+)\\)")
        private val WIDTH = Regex(pattern = "\\s*(\\d{1,5})(?:x\\d{1,5})?\\s*")
        private val WIDTH_AFTER_ALT = Regex(pattern = "\\|\\s*(\\d{1,5})(?:x\\d{1,5})?\\s*$")
        private val WIKI_EMBED = Regex(pattern = "!\\[\\[([^\\]|\\n]+)(?:\\|([^\\]\\n]*))?]]")
    }
}
