package app.notekea.editor.presentation.sidepanel

import androidx.compose.runtime.Immutable
import app.notekea.editor.presentation.markdown.HeadingPositions
import app.notekea.core.notes.domain.OutlineHeading

/** What Edit mode's text needs to say where the outline's [headings] are: the [positions] it fills in. */
@Immutable
internal data class OutlinePlacement(
    val headings: List<OutlineHeading>,
    val positions: HeadingPositions
)
