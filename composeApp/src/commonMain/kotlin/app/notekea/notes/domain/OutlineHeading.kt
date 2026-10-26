package app.notekea.notes.domain

/**
 * One heading of a note's outline (`ROADMAP.md` 1.9): its [level] (1 for `#`, up to 6), its [text] as a reader sees
 * it (without the `#` marks, emphasis or link brackets), the [offset] where its line starts in the note's body, and
 * its [depth] in the outline tree: how many earlier headings of a lower level hold it, so a `###` right under a `#` is
 * one step in, as in Obsidian.
 */
data class OutlineHeading(
    val level: Int,
    val text: String,
    val offset: Int,
    val depth: Int
)
