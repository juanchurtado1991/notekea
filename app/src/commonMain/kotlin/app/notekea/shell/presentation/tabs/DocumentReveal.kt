package app.notekea.shell.presentation.tabs

import app.notekea.core.notes.domain.RelativePath

/** A passage of the document at [path] to show. */
internal data class DocumentReveal(
    val path: RelativePath,
    val range: IntRange,
    val count: Int
)
