package app.notekea.notes.domain

/**
 * A note that names the open one (`ROADMAP.md` 2.5): [source], where it first does ([range], in its body: the whole
 * link, or the plain words), how many times it does ([count]), and the [passage] around it as a reader sees it (its
 * line without Markdown syntax, shortened around the mention) with the mention at [highlight].
 */
data class NoteMention(
    val source: RelativePath,
    val range: IntRange,
    val count: Int,
    val passage: String,
    val highlight: IntRange
)
