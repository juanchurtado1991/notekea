package app.notekea.editor.presentation.sidepanel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.notes.domain.SidePanelPreferences
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.layout.NotekeaSplitPane
import app.notekea.core.presentation.designsystem.components.layout.PaneSide
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.editor.presentation.noteinfo.NoteInfo
import kotlin.math.roundToInt

private const val EDITOR_MIN_WIDTH_DP = 480
private const val PREVIEW_CONTENT = "Notes"
private const val PREVIEW_HEIGHT_DP = 360
private const val PREVIEW_WIDTH_DP = 1000

/**
 * The [content] (the panes of notes) with the side panel on its right, as wide as [preferences] say but never leaving
 * the notes narrower than [EDITOR_MIN_WIDTH_DP]; when not even its narrowest width fits, it floats over them
 * ([sidePanel]). [current] changes as the active pane scrolls, so only the outline reads it.
 */
@Composable
fun WithSidePanel(
    preferences: SidePanelPreferences,
    sidePanel: SidePanelHost,
    outline: List<OutlineHeading>,
    current: () -> Int,
    platformConventions: PlatformConventions,
    onHeadingClick: (Int) -> Unit,
    onLeave: () -> Unit,
    info: NoteInfo?,
    modifier: Modifier = Modifier,
    inspector: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val actions = remember(sidePanel, onHeadingClick, onLeave) {
        actionsOf(
            sidePanel = sidePanel,
            onHeadingClick = onHeadingClick,
            onLeave = onLeave
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val room = maxWidth - EDITOR_MIN_WIDTH_DP.dp
        val isOverlay = room < SidePanelPreferences.MIN_WIDTH_DP.dp

        val maxPanelWidth =
            minOf(SidePanelPreferences.MAX_WIDTH_DP.dp, if (isOverlay) maxWidth else room)

        SideEffect {
            sidePanel.isOverlay = isOverlay
            if (!isOverlay) sidePanel.isOverlayOpen = false
        }

        NotekeaSplitPane(
            panelWidth = minOf(preferences.widthDp.dp, maxPanelWidth),
            onPanelWidthChange = {
                sidePanel.onIntent(
                    intent = EditorIntent.OnSidePanelResize(widthDp = it.value.roundToInt())
                )
            },
            isPanelVisible = if (isOverlay) sidePanel.isOverlayOpen else preferences.isVisible,
            minPanelWidth = SidePanelPreferences.MIN_WIDTH_DP.dp,
            maxPanelWidth = maxPanelWidth,
            panel = {
                SidePanel(
                    outline = outline,
                    current = current,
                    preferences = preferences,
                    platformConventions = platformConventions,
                    actions = actions,
                    info = info,
                    outlineFocus = sidePanel.outlineFocus,
                    inspector = inspector,
                    connections = sidePanel.connections,
                    chat = { sidePanel.chat(sidePanel.chatFocus) }
                )
            },
            content = content,
            panelSide = PaneSide.END,
            isOverlay = isOverlay
        )
    }
}

/**
 * The panel's controls: its modes, sections and minimize button go to [sidePanel]; Esc in the outline closes it if it
 * floats.
 */
private fun actionsOf(
    sidePanel: SidePanelHost,
    onHeadingClick: (Int) -> Unit,
    onLeave: () -> Unit
): SidePanelActions {
    fun leave() {
        sidePanel.isOverlayOpen = false
        onLeave()
    }

    return SidePanelActions(
        onHeadingClick = onHeadingClick,
        onModeSelect = {
            sidePanel.onIntent(intent = EditorIntent.OnSidePanelModeSelect(mode = it))
        },
        onSectionToggle = {
            sidePanel.onIntent(intent = EditorIntent.OnSidePanelSectionToggle(section = it))
        },
        onHide = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelToggle) },
        onLeave = { leave() }
    )
}

@Preview
@Composable
private fun WithSidePanelPreview() {
    NotekeaPreviewThemes {
        Box(
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            WithSidePanel(
                preferences = SidePanelPreferences.DEFAULT,
                sidePanel = remember {
                    SidePanelHost(send = {})
                },
                outline = EditorPreviewData.outline,
                current = { 0 },
                platformConventions = EditorPreviewData.platformConventions,
                onHeadingClick = {},
                onLeave = {},
                info = EditorPreviewData.info
            ) { Text(text = PREVIEW_CONTENT) }
        }
    }
}
