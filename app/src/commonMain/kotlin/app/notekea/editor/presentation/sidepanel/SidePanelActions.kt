package app.notekea.editor.presentation.sidepanel

import androidx.compose.runtime.Immutable
import app.notekea.core.notes.domain.SidePanelMode
import app.notekea.core.notes.domain.SidePanelSection

/**
 * What the side panel's controls do: a heading chosen in the outline ([onHeadingClick], by its index), Inspector or
 * Chat chosen ([onModeSelect]), a section's header clicked ([onSectionToggle]), the hide button ([onHide]), and Esc in
 * the outline ([onLeave]).
 */
@Immutable
class SidePanelActions(
    val onHeadingClick: (Int) -> Unit,
    val onModeSelect: (SidePanelMode) -> Unit,
    val onSectionToggle: (SidePanelSection) -> Unit,
    val onHide: () -> Unit,
    val onLeave: () -> Unit
)
