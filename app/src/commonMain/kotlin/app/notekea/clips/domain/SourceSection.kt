package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/** A clipped note as it is in the vault: its [path], its [text] and the page it came from ([source]). */
internal data class SourceNote(
    val path: RelativePath,
    val text: String,
    val source: String
)

/**
 * The notes of one clip: a page alone, or a section's [index] at `Web sources/<name>.md` with its pages in [folder].
 */
internal data class SourceSection(
    val folder: RelativePath,
    val notes: List<SourceNote>,
    val index: SourceNote?
)
