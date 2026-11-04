package app.notekea.notes.presentation.connections

import app.notekea.notes.domain.RelativePath

/**
 * What a row of the inspector's Connections can do (`ROADMAP.md` 2.4, 2.5): open its note, open it there at the place
 * it names the open note ([onReveal], the range in its body), open it beside the note, or link to it in the open note.
 */
class ConnectionActions(
    val onOpen: (RelativePath) -> Unit,
    val onReveal: (RelativePath, IntRange) -> Unit,
    val onOpenBeside: (RelativePath) -> Unit,
    val onInsertLink: (RelativePath) -> Unit
)
