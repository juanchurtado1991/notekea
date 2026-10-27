package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Immutable
import app.notekea.notes.domain.SidePanelSection

/**
 * What the side panel's controls do: a heading chosen in the outline ([onHeadingClick], by its index), a section's
 * header clicked ([onSectionToggle]), the hide button ([onHide]), and Esc in the outline ([onLeave]).
 */
@Immutable
class SidePanelActions(
    val onHeadingClick: (Int) -> Unit,
    val onSectionToggle: (SidePanelSection) -> Unit,
    val onHide: () -> Unit,
    val onLeave: () -> Unit
)
