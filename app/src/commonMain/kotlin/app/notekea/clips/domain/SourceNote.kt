package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/** A clipped note as it is in the vault: its [path], its [text] and the page it came from ([source]). */
internal data class SourceNote(
    val path: RelativePath,
    val text: String,
    val source: String
)
