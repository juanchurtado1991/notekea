package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.LinkKind

/** A link clicked in Read mode: to a note of the vault, or to anything else (a web page, an email address). */
sealed interface ReadLink {
    /** [target] is the link's target only (no alias or heading); [kind] says how to resolve it. */
    data class Note(
        val target: String,
        val kind: LinkKind
    ) : ReadLink

    data class External(val uri: String) : ReadLink
}
