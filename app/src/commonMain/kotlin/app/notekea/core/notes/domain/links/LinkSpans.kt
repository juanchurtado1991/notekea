package app.notekea.core.notes.domain.links

import app.notekea.core.notes.domain.RelativePath

/*
 * Where a whole link sits in a note's text, around the target a [NoteLink] covers, and how a wiki link is written again
 * as a Markdown link when the note it names can no longer be named by one.
 */

private const val BAR = "|"
private const val BLOCK = '^'
private const val EMBED = '!'
private const val ESCAPE = '\\'
private const val HEADING = "#"
private const val MARKDOWN_CLOSE = ')'
private const val MARKDOWN_MIDDLE = "]("
private const val MARKDOWN_OPEN = '['
private const val WIKI_CLOSE = "]]"
private const val WIKI_OPEN = "[["

/** The whole of [link] in [body], from its first bracket (or the `!` of an embed) to its last. */
fun linkSpan(
    body: String,
    link: NoteLink
): IntRange = when (link.kind) {
    LinkKind.WIKI -> {
        val open = body.lastIndexOf(
            string = WIKI_OPEN,
            startIndex = link.targetStart
        )

        val start = if (open > 0 && body[open - 1] == EMBED) open - 1 else open

        start..(
            body.indexOf(
                string = WIKI_CLOSE,
                startIndex = link.targetEnd
            ) + 1
            )
    }

    LinkKind.MARKDOWN_ANGLE, LinkKind.MARKDOWN_ENCODED -> {
        val middle = body.lastIndexOf(
            string = MARKDOWN_MIDDLE,
            startIndex = link.targetStart
        )

        val start = openingBracket(
            body = body,
            before = middle
        )

        start..body.indexOf(
            char = MARKDOWN_CLOSE,
            startIndex = link.targetEnd
        )
    }
}

/**
 * The wiki [link] in [body] as a Markdown link from [source] to [target], keeping its embed, heading or block and
 * alias: `![[C# notes#Steps|the notes]]` becomes `![the notes](C%23%20notes.md#Steps)`.
 */
fun wikiAsMarkdown(
    body: String,
    link: NoteLink,
    target: RelativePath,
    source: RelativePath
): String {
    val span = linkSpan(
        body = body,
        link = link
    )

    // What follows the target inside the brackets: `#heading` or `^block`, then `|alias` (`\|` in a table).
    val rest = body.substring(
        startIndex = link.targetEnd,
        endIndex = span.last - 1
    )

    val alias = rest.substringAfter(
        delimiter = BAR,
        missingDelimiterValue = ""
    )

    val anchor = rest
        .substringBefore(delimiter = BAR)
        .removeSuffix(suffix = ESCAPE.toString())
        .removePrefix(prefix = HEADING)

    val fragment = when {
        anchor.isEmpty() -> ""
        anchor.first() == BLOCK -> HEADING + anchor
        else -> HEADING + spacesEncoded(target = anchor)
    }

    val markdown = markdownLink(
        label = alias.ifBlank { target.nameWithoutExtension },
        target = target,
        source = source,
        fragment = fragment
    )

    return if (body[span.first] == EMBED) EMBED + markdown else markdown
}

/** The `[` that opens a Markdown link's text ending [before], skipping brackets escaped inside the text. */
private fun openingBracket(
    body: String,
    before: Int
): Int {
    var index = before + 1

    do {
        index = body.lastIndexOf(
            char = MARKDOWN_OPEN,
            startIndex = index - 1
        )
    } while (isEscaped(body = body, index = index))

    return index
}

private fun isEscaped(
    body: String,
    index: Int
): Boolean = index > 0 && body[index - 1] == ESCAPE
