package app.notekea.notes.domain

/**
 * A file shown inside a note (`ROADMAP.md` 1.8): [range] is the whole embed in the text, [target] the file as written
 * (decoded for a Markdown link), [width] the width it asks for (`|300`), [label] its alt text or caption, and
 * [isOwnLine] whether nothing else is on its line, so it can be shown as a block.
 */
data class Embed(
    val range: IntRange,
    val target: String,
    val kind: EmbedKind,
    val isOwnLine: Boolean,
    val label: String? = null,
    val width: Int? = null
)
