package app.notekea.graph.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.components.fields.NotekeaCheckbox
import app.notekea.core.presentation.designsystem.components.tabs.NotekeaCountTabs
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.platform.label
import app.notekea.graph.presentation.model.GraphIntent
import app.notekea.graph.presentation.model.GraphPreviewData
import app.notekea.graph.presentation.model.GraphScope
import app.notekea.graph.presentation.model.GraphState
import app.notekea.resources.Res
import app.notekea.resources.graph_depth_one
import app.notekea.resources.graph_depth_two
import app.notekea.resources.graph_find
import app.notekea.resources.graph_orphans
import app.notekea.resources.graph_related
import app.notekea.resources.graph_this_note
import app.notekea.resources.graph_whole_vault
import org.jetbrains.compose.resources.stringResource

private const val SCOPE_WIDTH_DP = 200
private const val DEPTH_WIDTH_DP = 72
private const val MAX_DEPTH = 2

/** Around a note or the whole vault, how deep, what else shows, and search. */
@Composable
internal fun GraphToolbar(
    state: GraphState,
    platformConventions: PlatformConventions,
    onIntent: (GraphIntent) -> Unit
) {
    val isAround = state.scope == GraphScope.THIS_NOTE

    Row(
        modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScopeTabs(
            state = state,
            onIntent = onIntent
        )

        if (isAround) {
            DepthTabs(
                state = state,
                onIntent = onIntent
            )
        }

        Toggle(
            label = stringResource(
                if (isAround) Res.string.graph_related else Res.string.graph_orphans
            ),
            isChecked = if (isAround) state.showsRelated else state.showsOrphans,
            onClick = {
                onIntent(if (isAround) GraphIntent.OnRelatedToggle else GraphIntent.OnOrphansToggle)
            }
        )

        Spacer(modifier = Modifier.weight(weight = 1f))

        NotekeaIconButton(
            icon = NotekeaIcons.Find,
            label = stringResource(resource = Res.string.graph_find),
            onClick = { onIntent(GraphIntent.OnSearchOpen) },
            shortcutHint = platformConventions.findShortcut.label(
                operatingSystem = platformConventions.operatingSystem
            )
        )
    }
}

@Composable
private fun ScopeTabs(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit
) {
    NotekeaCountTabs(
        labels = listOf(
            stringResource(resource = Res.string.graph_this_note),
            stringResource(resource = Res.string.graph_whole_vault)
        ),
        counts = listOf(null, null),
        selectedIndex = state.scope.ordinal,
        onSelect = { onIntent(GraphIntent.OnScopeSelect(scope = GraphScope.entries[it])) },
        modifier = Modifier.width(width = SCOPE_WIDTH_DP.dp)
    )
}

@Composable
private fun DepthTabs(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit
) {
    NotekeaCountTabs(
        labels = listOf(
            stringResource(resource = Res.string.graph_depth_one),
            stringResource(resource = Res.string.graph_depth_two)
        ),
        counts = listOf(null, null),
        selectedIndex = (state.depth - 1).coerceIn(
            minimumValue = 0,
            maximumValue = MAX_DEPTH - 1
        ),
        onSelect = { onIntent(GraphIntent.OnDepthSelect(depth = it + 1)) },
        modifier = Modifier.width(width = DEPTH_WIDTH_DP.dp)
    )
}

@Composable
private fun Toggle(
    label: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        NotekeaCheckbox(
            checked = isChecked,
            onClick = onClick,
            label = label
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Preview
@Composable
private fun GraphToolbarPreview() {
    NotekeaPreviewThemes {
        GraphToolbar(
            state = GraphPreviewData.state,
            platformConventions = PreviewPlatformConventions.linux,
            onIntent = {}
        )
    }
}
