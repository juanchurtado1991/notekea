package app.notekea.connections.presentation

import app.notekea.core.notes.domain.RelativePath

/**
 * What a row of the inspector's Connections can do: open its note, open it there at the place
 * it names the open note ([onReveal], the range in its body), open it beside the note, or link to it in the open note.
 */
class ConnectionActions(
    val onOpen: (RelativePath) -> Unit,
    val onReveal: (RelativePath, IntRange) -> Unit,
    val onOpenBeside: (RelativePath) -> Unit,
    val onInsertLink: (RelativePath) -> Unit,
    // A passage shown to be read (a source cited in the chat): selected, its lines not put into editing.
    val onShow: (RelativePath, IntRange) -> Unit = onReveal
)
