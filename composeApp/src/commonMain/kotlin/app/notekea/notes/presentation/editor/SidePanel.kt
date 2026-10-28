package app.notekea.notes.presentation.editor

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaFold
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaSectionHeader
import app.notekea.core.presentation.components.NotekeaSegment
import app.notekea.core.presentation.components.NotekeaSegmentedControl
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.SidePanelMode
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelSection
import app.notekea.resources.Res
import app.notekea.resources.note_info_title
import app.notekea.resources.outline_title
import app.notekea.resources.side_panel_chat
import app.notekea.resources.side_panel_inspector
import app.notekea.resources.side_panel_minimize
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 480
private const val PREVIEW_WIDTH_DP = 300

/**
 * The panel on the right of the editor (`ROADMAP.md` 1.9), in the mode [preferences] keep: Inspector | Chat chosen at
 * its top beside the button that minimizes it. The inspector's sections fold on their own ([preferences]'s folded
 * sections; a header's click goes to the actions). Outline lists the note's headings as an indented tree, highlights
 * [current], the one the editor's view is in, and goes to a heading when it is clicked; a note without headings says
 * how to add one. The list is one focus stop, taken with [outlineFocus] (`Mod+Shift+O`): the arrows move, Enter goes
 * to the heading and Esc gives the keyboard back to the note. Note info, shown while a note is open ([info]), says what
 * it holds and when it was created and modified. Chat says it is coming (`ROADMAP.md` 3.3).
 */
@Composable
fun SidePanel(
    outline: List<OutlineHeading>,
    current: Int,
    preferences: SidePanelPreferences,
    platformConventions: PlatformConventions,
    actions: SidePanelActions,
    info: NoteInfo?,
    modifier: Modifier = Modifier,
    outlineFocus: FocusRequester = remember { FocusRequester() }
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(vertical = NotekeaDimensions.SpacingXs)) {
            PanelHeader(mode = preferences.mode, platformConventions = platformConventions, actions = actions)
            // Inspector and Chat fade into each other, so the switch reads as one panel changing.
            Crossfade(
                targetState = preferences.mode,
                animationSpec = tween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)
            ) { mode ->
                when (mode) {
                    SidePanelMode.INSPECTOR -> InspectorSections(
                        outline = outline,
                        current = current,
                        foldedSections = preferences.foldedSections,
                        platformConventions = platformConventions,
                        actions = actions,
                        outlineFocus = outlineFocus,
                        info = info
                    )

                    SidePanelMode.CHAT -> SidePanelChat()
                }
            }
        }
    }
}

/** Inspector | Chat, and the button that minimizes the panel. */
@Composable
private fun PanelHeader(
    mode: SidePanelMode,
    platformConventions: PlatformConventions,
    actions: SidePanelActions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = NotekeaDimensions.SpacingS,
                end = NotekeaDimensions.SpacingXs,
                bottom = NotekeaDimensions.SpacingXs
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaSegmentedControl(
            segments = listOf(
                NotekeaSegment(icon = NotekeaIcons.Inspector, label = stringResource(Res.string.side_panel_inspector)),
                NotekeaSegment(icon = NotekeaIcons.Chat, label = stringResource(Res.string.side_panel_chat))
            ),
            selectedIndex = SidePanelMode.entries.indexOf(element = mode),
            onSelect = { actions.onModeSelect(SidePanelMode.entries[it]) }
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
        MinimizeButton(platformConventions = platformConventions, onMinimize = actions.onHide)
    }
}

/** The inspector's sections: Outline, and Note info while a note is open. */
@Composable
private fun InspectorSections(
    outline: List<OutlineHeading>,
    current: Int,
    foldedSections: Set<SidePanelSection>,
    platformConventions: PlatformConventions,
    actions: SidePanelActions,
    outlineFocus: FocusRequester,
    info: NoteInfo?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val isOutlineOpen = SidePanelSection.OUTLINE !in foldedSections
        NotekeaSectionHeader(
            title = stringResource(resource = Res.string.outline_title),
            isOpen = isOutlineOpen,
            onToggle = { actions.onSectionToggle(SidePanelSection.OUTLINE) },
            shortcutHint = platformConventions.outlineShortcut.label(
                operatingSystem = platformConventions.operatingSystem
            )
        )
        NotekeaFold(isOpen = isOutlineOpen, modifier = Modifier.weight(weight = 1f, fill = false)) {
            OutlineSection(outline = outline, current = current, focusRequester = outlineFocus, actions = actions)
        }
        info?.let { NoteInfoSection(info = it, isOpen = SidePanelSection.NOTE_INFO !in foldedSections, actions) }
    }
}

@Composable
private fun MinimizeButton(
    platformConventions: PlatformConventions,
    onMinimize: () -> Unit
) {
    NotekeaIconButton(
        icon = NotekeaIcons.Minimize,
        label = stringResource(resource = Res.string.side_panel_minimize),
        onClick = onMinimize,
        shortcutHint = platformConventions.toggleSidePanelShortcut.label(
            operatingSystem = platformConventions.operatingSystem
        ),
        takesFocus = false
    )
}

/** A hairline between sections, then Note info: its rows while it is open. */
@Composable
private fun NoteInfoSection(
    info: NoteInfo,
    isOpen: Boolean,
    actions: SidePanelActions
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingS)
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outline)
    )
    NotekeaSectionHeader(
        title = stringResource(resource = Res.string.note_info_title),
        isOpen = isOpen,
        onToggle = { actions.onSectionToggle(SidePanelSection.NOTE_INFO) }
    )
    NotekeaFold(isOpen = isOpen) { NoteInfoRows(info = info) }
}

@Composable
private fun SidePanelPreviewFrame(
    outline: List<OutlineHeading>,
    current: Int,
    foldedSections: Set<SidePanelSection> = emptySet(),
    mode: SidePanelMode = SidePanelMode.INSPECTOR
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            SidePanel(
                outline = outline,
                current = current,
                preferences = SidePanelPreferences(mode = mode, foldedSections = foldedSections),
                platformConventions = EditorPreviewData.platformConventions,
                actions = SidePanelActions(
                    onHeadingClick = {},
                    onModeSelect = {},
                    onSectionToggle = {},
                    onHide = {},
                    onLeave = {}
                ),
                info = EditorPreviewData.info
            )
        }
    }
}

@Preview
@Composable
private fun SidePanelPreview() {
    SidePanelPreviewFrame(outline = EditorPreviewData.outline, current = 1)
}

@Preview
@Composable
private fun SidePanelEmptyPreview() {
    SidePanelPreviewFrame(outline = emptyList(), current = OutlineTracker.NONE)
}

@Preview
@Composable
private fun SidePanelChatModePreview() {
    SidePanelPreviewFrame(outline = EditorPreviewData.outline, current = 1, mode = SidePanelMode.CHAT)
}

@Preview
@Composable
private fun SidePanelFoldedPreview() {
    SidePanelPreviewFrame(
        outline = EditorPreviewData.outline,
        current = 1,
        foldedSections = setOf(SidePanelSection.OUTLINE)
    )
}
