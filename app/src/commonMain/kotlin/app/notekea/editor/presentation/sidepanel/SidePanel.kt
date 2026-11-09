package app.notekea.editor.presentation.sidepanel

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.notes.domain.SidePanelMode
import app.notekea.core.notes.domain.SidePanelPreferences
import app.notekea.core.notes.domain.SidePanelSection
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaFold
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.components.NotekeaSectionHeader
import app.notekea.core.presentation.designsystem.components.NotekeaSegment
import app.notekea.core.presentation.designsystem.components.NotekeaSegmentedControl
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.designsystem.theme.motionTween
import app.notekea.core.presentation.platform.label
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.editor.presentation.noteinfo.NoteInfo
import app.notekea.editor.presentation.noteinfo.NoteInfoRows
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
 * The panel on the right of the editor, in the mode [preferences] keep: Inspector | Chat. The inspector's sections fold
 * on their own. Outline highlights [current] and is one focus stop, taken with [outlineFocus]: the arrows move, Enter
 * goes to the heading and Esc gives the keyboard back. A tab that is no note shows its own [inspector].
 */
@Composable
fun SidePanel(
    outline: List<OutlineHeading>,
    current: () -> Int,
    preferences: SidePanelPreferences,
    platformConventions: PlatformConventions,
    actions: SidePanelActions,
    info: NoteInfo?,
    modifier: Modifier = Modifier,
    outlineFocus: FocusRequester = remember { FocusRequester() },
    inspector: (@Composable () -> Unit)? = null,
    connections: @Composable ColumnScope.(
        isOpen: Boolean,
        onToggle: () -> Unit
    ) -> Unit = { _, _ -> }
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(vertical = NotekeaDimensions.SpacingXs)) {
            PanelHeader(
                mode = preferences.mode,
                platformConventions = platformConventions,
                actions = actions
            )

            // Inspector and Chat fade into each other, so the switch reads as one panel changing.
            Crossfade(
                targetState = preferences.mode,
                animationSpec = motionTween(durationMillis = NotekeaDimensions.MOTION_SHORT_MILLIS)
            ) { mode ->
                when {
                    mode == SidePanelMode.CHAT -> SidePanelChat()

                    inspector != null -> inspector()

                    else -> InspectorSections(
                        outline = outline,
                        current = current,
                        foldedSections = preferences.foldedSections,
                        platformConventions = platformConventions,
                        actions = actions,
                        outlineFocus = outlineFocus,
                        info = info,
                        connections = connections
                    )
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
                NotekeaSegment(
                    icon = NotekeaIcons.Inspector,
                    label = stringResource(Res.string.side_panel_inspector)
                ),
                NotekeaSegment(
                    icon = NotekeaIcons.Chat,
                    label = stringResource(Res.string.side_panel_chat)
                )
            ),
            selectedIndex = SidePanelMode.entries.indexOf(element = mode),
            onSelect = { actions.onModeSelect(SidePanelMode.entries[it]) }
        )

        Spacer(modifier = Modifier.weight(weight = 1f))

        MinimizeButton(
            platformConventions = platformConventions,
            onMinimize = actions.onHide
        )
    }
}

/** The inspector's sections: Outline, and Connections and Note info while a note is open. */
@Composable
private fun InspectorSections(
    outline: List<OutlineHeading>,
    current: () -> Int,
    foldedSections: Set<SidePanelSection>,
    platformConventions: PlatformConventions,
    actions: SidePanelActions,
    outlineFocus: FocusRequester,
    info: NoteInfo?,
    connections: @Composable ColumnScope.(isOpen: Boolean, onToggle: () -> Unit) -> Unit
) {
    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            OutlinePart(
                outline = outline,
                current = current,
                isOpen = SidePanelSection.OUTLINE !in foldedSections,
                platformConventions = platformConventions,
                actions = actions,
                outlineFocus = outlineFocus
            )

            if (info != null) {
                Column {
                    connections(SidePanelSection.CONNECTIONS !in foldedSections) {
                        actions.onSectionToggle(SidePanelSection.CONNECTIONS)
                    }
                }

                Column {
                    NoteInfoSection(
                        info = info,
                        isOpen = SidePanelSection.NOTE_INFO !in foldedSections,
                        actions = actions
                    )
                }
            }
        },
        measurePolicy = InspectorMeasurePolicy
    )
}

/** Outline's header and, while [isOpen], its headings. */
@Composable
private fun OutlinePart(
    outline: List<OutlineHeading>,
    current: () -> Int,
    isOpen: Boolean,
    platformConventions: PlatformConventions,
    actions: SidePanelActions,
    outlineFocus: FocusRequester
) {
    Column {
        NotekeaSectionHeader(
            title = stringResource(resource = Res.string.outline_title),
            isOpen = isOpen,
            onToggle = { actions.onSectionToggle(SidePanelSection.OUTLINE) },
            shortcutHint = platformConventions.outlineShortcut.label(
                operatingSystem = platformConventions.operatingSystem
            )
        )

        NotekeaFold(
            isOpen = isOpen,
            modifier = Modifier.weight(
                weight = 1f,
                fill = false
            )
        ) {
            OutlineSection(
                outline = outline,
                current = current,
                focusRequester = outlineFocus,
                actions = actions
            )
        }
    }
}

/**
 * Outline, then Connections and Note info when a note is open, stacked. Note info takes what it needs; Connections take
 * up to half of the rest, or more when the outline needs less, which it says without being measured (Connections
 * cannot: their rows are lazy); Outline takes what is left. Each scrolls inside its share.
 */
private val InspectorMeasurePolicy = MeasurePolicy { measurables, constraints ->
    val loose = constraints.copy(minHeight = 0)
    val outline = measurables.first()
    val connections = measurables.getOrNull(index = 1)
    val info = measurables.getOrNull(index = 2)?.measure(constraints = loose)
    val free = (constraints.maxHeight - (info?.height ?: 0)).coerceAtLeast(minimumValue = 0)
    val outlineNeed = outline.maxIntrinsicHeight(width = constraints.maxWidth)

    val middle = connections?.measure(
        constraints = loose.copy(
            maxHeight = maxOf(
                free / 2,
                free - outlineNeed
            )
        )
    )

    val top = outline.measure(constraints = loose.copy(maxHeight = free - (middle?.height ?: 0)))

    layout(
        width = constraints.maxWidth,
        height = constraints.maxHeight
    ) {
        top.placeRelative(
            x = 0,
            y = 0
        )

        middle?.placeRelative(
            x = 0,
            y = top.height
        )

        info?.placeRelative(
            x = 0,
            y = top.height + (middle?.height ?: 0)
        )
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
            .padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingS
            )
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
    current: () -> Int,
    foldedSections: Set<SidePanelSection> = emptySet(),
    mode: SidePanelMode = SidePanelMode.INSPECTOR
) {
    NotekeaPreviewThemes {
        Box(
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            SidePanel(
                outline = outline,
                current = current,
                preferences = SidePanelPreferences(
                    mode = mode,
                    foldedSections = foldedSections
                ),
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
    SidePanelPreviewFrame(
        outline = EditorPreviewData.outline,
        current = { 1 }
    )
}

@Preview
@Composable
private fun SidePanelEmptyPreview() {
    SidePanelPreviewFrame(
        outline = emptyList(),
        current = { OutlineTracker.NONE }
    )
}

@Preview
@Composable
private fun SidePanelChatModePreview() {
    SidePanelPreviewFrame(
        outline = EditorPreviewData.outline,
        current = { 1 },
        mode = SidePanelMode.CHAT
    )
}

@Preview
@Composable
private fun SidePanelFoldedPreview() {
    SidePanelPreviewFrame(
        outline = EditorPreviewData.outline,
        current = { 1 },
        foldedSections = setOf(SidePanelSection.OUTLINE)
    )
}
