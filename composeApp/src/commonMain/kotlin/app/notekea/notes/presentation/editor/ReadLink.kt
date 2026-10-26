package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.LinkKind

/**
 * A link clicked in Read mode: to a note of the vault, a `#tag` (searched in all notes), a file embedded in a
 * paragraph, or anything else (a web page, an email address).
 */
sealed interface ReadLink {
    /** [target] is the link's target only (no alias or heading); [kind] says how to resolve it. */
    data class Note(
        val target: String,
        val kind: LinkKind
    ) : ReadLink

    data class External(val uri: String) : ReadLink

    /** [name] is the tag without `#` (`recipe/venezuelan`). */
    data class Tag(val name: String) : ReadLink

    /** A file embedded inside a paragraph (`![[menu.pdf]]`), as the note writes its [target]. */
    data class File(val target: String) : ReadLink
}
