package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaSplitPane
import app.notekea.core.presentation.components.PaneSide
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.SidePanelPreferences
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.remember

private const val EDITOR_MIN_WIDTH_DP = 480
private const val PREVIEW_CONTENT = "Notes"
private const val PREVIEW_HEIGHT_DP = 360
private const val PREVIEW_WIDTH_DP = 1000

/**
 * The [content] (the panes of notes) with the side panel on its right showing [outline] (`ROADMAP.md` 1.9) and the
 * active note's [info] (null when no note is shown), its sections folded as [preferences] say, as wide as
 * [preferences] say but never leaving the notes narrower than [EDITOR_MIN_WIDTH_DP]; when not even its narrowest width
 * fits, the panel floats over them instead, open only while it is used ([sidePanel]). [current] is the heading the
 * active pane's view is in; a heading chosen goes to [onHeadingClick], and Esc in the outline to [onLeave].
 */
@Composable
fun WithSidePanel(
    preferences: SidePanelPreferences,
    sidePanel: SidePanelHost,
    outline: List<OutlineHeading>,
    current: Int,
    platformConventions: PlatformConventions,
    onHeadingClick: (Int) -> Unit,
    onLeave: () -> Unit,
    info: NoteInfo?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val room = maxWidth - EDITOR_MIN_WIDTH_DP.dp
        val isOverlay = room < SidePanelPreferences.MIN_WIDTH_DP.dp
        val maxPanelWidth = minOf(SidePanelPreferences.MAX_WIDTH_DP.dp, if (isOverlay) maxWidth else room)
        SideEffect {
            sidePanel.isOverlay = isOverlay
            if (!isOverlay) sidePanel.isOverlayOpen = false
        }
        NotekeaSplitPane(
            panelWidth = minOf(preferences.widthDp.dp, maxPanelWidth),
            onPanelWidthChange = {
                sidePanel.onIntent(intent = EditorIntent.OnSidePanelResize(widthDp = it.value.roundToInt()))
            },
            isPanelVisible = if (isOverlay) sidePanel.isOverlayOpen else preferences.isVisible,
            minPanelWidth = SidePanelPreferences.MIN_WIDTH_DP.dp,
            maxPanelWidth = maxPanelWidth,
            panel = {
                SidePanel(
                    outline = outline,
                    current = current,
                    foldedSections = preferences.foldedSections,
                    platformConventions = platformConventions,
                    actions = actionsOf(sidePanel = sidePanel, onHeadingClick = onHeadingClick, onLeave = onLeave),
                    info = info,
                    outlineFocus = sidePanel.outlineFocus
                )
            },
            content = content,
            panelSide = PaneSide.END,
            isOverlay = isOverlay
        )
    }
}

/** The panel's controls: its sections and hide button go to [sidePanel]; Esc in the outline closes it if it floats. */
private fun actionsOf(
    sidePanel: SidePanelHost,
    onHeadingClick: (Int) -> Unit,
    onLeave: () -> Unit
) = SidePanelActions(
    onHeadingClick = onHeadingClick,
    onSectionToggle = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelSectionToggle(section = it)) },
    onHide = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelToggle) },
    onLeave = {
        sidePanel.isOverlayOpen = false
        onLeave()
    }
)

@Preview
@Composable
private fun WithSidePanelPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            WithSidePanel(
                preferences = SidePanelPreferences.DEFAULT,
                sidePanel = remember { SidePanelHost(send = {}) },
                outline = EditorPreviewData.outline,
                current = 0,
                platformConventions = EditorPreviewData.platformConventions,
                onHeadingClick = {},
                onLeave = {},
                info = EditorPreviewData.info
            ) { Text(text = PREVIEW_CONTENT) }
        }
    }
}
