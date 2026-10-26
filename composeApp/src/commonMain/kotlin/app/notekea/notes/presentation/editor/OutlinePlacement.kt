package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Immutable
import app.notekea.notes.domain.OutlineHeading

/** What Edit mode's text needs to say where the outline's [headings] are: the [positions] it fills in. */
@Immutable
internal data class OutlinePlacement(
    val headings: List<OutlineHeading>,
    val positions: HeadingPositions
)
