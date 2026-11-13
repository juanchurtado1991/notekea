package app.notekea.editor.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.YouTubeLinks
import app.notekea.core.notes.domain.links.percentDecoded

/**
 * The embedded files of a note's text, as Obsidian reads them: `![[file|300]]` (a width or a caption), `![alt](path)`
 * (in `<…>` or percent-encoded, `|300` after the alt) and `![Title](https://youtu.be/…)`. Notes embedded are found
 * only when asked for, marked [Embed.isNote]; anything in code is left out.
 */
class EmbedFinder {
    fun embeds(
        text: String,
        includeNotes: Boolean = false
    ): List<Embed> {
        val embeds = mutableListOf<Embed>()
        val fences = FenceTracker()
        var lineStart = 0

        text.split(StringPatterns.NEW_LINE_CHAR).forEach { line ->
            if (!fences.read(text = line)) {
                embeds += embedsOf(
                    line = line,
                    offset = lineStart
                )
            }

            lineStart += line.length + 1
        }

        return embeds.filter {
            val isShown = isFile(target = it.target) || isYouTube(target = it.target)
            if (it.isNote) includeNotes else isShown
        }
    }

    private fun embedsOf(
        line: String,
        offset: Int
    ): List<Embed> {
        val code = INLINE_CODE
            .findAll(input = line)
            .map { it.range }
            .toList()

        val found = WIKI_EMBED.findAll(input = line).map {
            wikiEmbed(
                match = it,
                line = line,
                offset = offset
            )
        } +
            MARKDOWN_EMBED.findAll(input = line).map {
                markdownEmbed(
                    match = it,
                    line = line,
                    offset = offset
                )
            }

        return found
            .filter { embed ->
                code.none {
                    it.first <= embed.range.first - offset && embed.range.last - offset <= it.last
                }
            }
            .sortedBy { it.range.first }
            .toList()
    }

    private fun wikiEmbed(
        match: MatchResult,
        line: String,
        offset: Int
    ): Embed {
        val option = match.groupValues[2].takeIf {
            match.groups[2] != null
        }

        val target = match
            .groupValues[1]
            .removeSuffix(suffix = TABLE_ESCAPE)
            .trim()

        return Embed(
            range = match.range.first + offset..match.range.last + offset,
            // In a table the `|` before a width is escaped (`![[photo.png\|80]]`), so the target ends with the `\`.
            target = target,
            kind = EmbedKind.WIKI,
            isOwnLine = line.trim() == match.value,
            label = option?.takeIf {
                WIDTH.matchEntire(input = it) == null
            },
            width = option?.let {
                WIDTH
                    .matchEntire(input = it)
                    ?.groupValues
                    ?.get(index = 1)
                    ?.toInt()
            },
            isNote = !isFile(target = target)
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

        val isInAngles =
            written.startsWith(prefix = ANGLE_OPEN) && written.endsWith(suffix = ANGLE_CLOSE)

        val target = if (isInAngles) {
            written.removePrefix(prefix = ANGLE_OPEN).removeSuffix(suffix = ANGLE_CLOSE)
        } else {
            percentDecoded(target = written)
        }

        val label = (
            if (width != null) {
                alt.removeRange(range = width.range)
            } else {
                alt
            }
            ).ifBlank { null }

        return Embed(
            range = match.range.first + offset..match.range.last + offset,
            target = target,
            kind = EmbedKind.MARKDOWN,
            isOwnLine = line.trim() == match.value,
            label = label,
            width = width
                ?.groupValues
                ?.get(index = 1)
                ?.toInt()
        )
    }

    // A YouTube video, shown as its card (`![Title](https://youtu.be/…)`, as Obsidian embeds it).
    private fun isYouTube(target: String): Boolean = YouTubeLinks.idOf(url = target) != null

    /** A file, not a note: its name has an extension other than `.md`. */
    private fun isFile(target: String): Boolean {
        val name = target.substringBefore(delimiter = MarkdownSyntax.HEADING_ANCHOR_CHAR)
            .substringAfterLast(delimiter = RelativePath.SEPARATOR)

        return EXTENSION.containsMatchIn(input = name) &&
            !name.endsWith(suffix = RelativePath.MARKDOWN_EXTENSION, ignoreCase = true)
    }

    companion object {
        private const val ANGLE_CLOSE = ">"
        private const val ANGLE_OPEN = "<"
        private const val TABLE_ESCAPE = "\\"
        private val EXTENSION = Regex(pattern = "\\.[A-Za-z0-9]{1,8}$")
        private val INLINE_CODE = Regex(pattern = "(`+).+?\\1")
        private val MARKDOWN_EMBED = Regex(pattern = "!\\[([^\\]\\n]*)]\\(([^)\\n]+)\\)")
        private val WIDTH = Regex(pattern = "\\s*(\\d{1,5})(?:x\\d{1,5})?\\s*")
        private val WIDTH_AFTER_ALT = Regex(pattern = "\\|\\s*(\\d{1,5})(?:x\\d{1,5})?\\s*$")
        private val WIKI_EMBED = Regex(pattern = "!\\[\\[([^\\]|\\n]+)(?:\\|([^\\]\\n]*))?]]")
    }
}
