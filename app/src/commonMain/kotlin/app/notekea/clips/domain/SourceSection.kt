package app.notekea.clips.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * The notes of one clip: a page alone, or a section's [index] at `Web sources/<name>.md` with its pages in [folder];
 * [names] are those of every note there, clipped or the person's own, which a new page's note never takes.
 */
internal data class SourceSection(
    val folder: RelativePath,
    val notes: List<SourceNote>,
    val index: SourceNote?,
    val names: Set<String> = emptySet()
)
