package app.notekea.notes.domain

/**
 * A link found in a note's text. [targetStart] and [targetEnd] (exclusive) cover only the target, so rewriting it keeps
 * the alias, heading, block reference and link text around it untouched.
 */
data class NoteLink(
    val targetStart: Int,
    val targetEnd: Int,
    val target: String,
    val kind: LinkKind
)
