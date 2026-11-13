package app.notekea.connections.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.sidepanel.ConnectionsTab
import app.notekea.core.presentation.designsystem.components.NotekeaSectionHeader
import app.notekea.core.presentation.designsystem.components.layout.NotekeaFold
import app.notekea.core.presentation.designsystem.components.tabs.NotekeaCountTabs
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.connections_linked
import app.notekea.resources.connections_related
import app.notekea.resources.connections_title
import app.notekea.resources.connections_unlinked
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 240

/**
 * The inspector's Connections: its header ([isOpen], [onToggle]), then a picker of
 * what it shows, each with how many: Related (by meaning, [related]), Linked and Unlinked (the notes that name the open
 * one, [mentions]); the chosen one ([tab], [onTabSelect]) lists below and scrolls when it does not fit.
 */
@Composable
fun ColumnScope.ConnectionsSection(
    tab: ConnectionsTab,
    onTabSelect: (ConnectionsTab) -> Unit,
    isOpen: Boolean,
    onToggle: () -> Unit,
    related: RelatedNotesState,
    mentions: NoteMentionsState,
    onRelatedIntent: (RelatedNotesIntent) -> Unit,
    onMentionsIntent: (NoteMentionsIntent) -> Unit,
    actions: ConnectionActions
) {
    NotekeaSectionHeader(
        title = stringResource(Res.string.connections_title),
        isOpen = isOpen,
        onToggle = onToggle
    )

    NotekeaFold(
        isOpen = isOpen,
        modifier = Modifier.weight(
            weight = 1f,
            fill = false
        )
    ) {
        Column {
            NotekeaCountTabs(
                labels = listOf(
                    stringResource(Res.string.connections_related),
                    stringResource(Res.string.connections_linked),
                    stringResource(Res.string.connections_unlinked)
                ),
                counts = countsOf(
                    related = related,
                    mentions = mentions
                ),
                selectedIndex = tab.ordinal,
                onSelect = { onTabSelect(ConnectionsTab.entries[it]) },
                modifier = Modifier.padding(
                    horizontal = NotekeaDimensions.SpacingM,
                    vertical = NotekeaDimensions.SpacingXs
                )
            )

            key(tab) {
                when (tab) {
                    ConnectionsTab.RELATED -> RelatedList(
                        state = related,
                        onIntent = onRelatedIntent,
                        actions = actions
                    )

                    ConnectionsTab.LINKED, ConnectionsTab.UNLINKED -> MentionList(
                        state = mentions,
                        isLinked = tab == ConnectionsTab.LINKED,
                        onIntent = onMentionsIntent,
                        actions = actions
                    )
                }
            }
        }
    }
}

/** How many each tab lists, or none while it is not known yet (the index not ready, mentions being found). */
private fun countsOf(
    related: RelatedNotesState,
    mentions: NoteMentionsState
): List<Int?> {
    val isFinding = mentions.isLoading || mentions.searchFailed

    return listOf(
        related
            .related
            .size
            .takeIf {
                related.isIndexReady && !related.isLoading
            },
        mentions
            .mentions
            .linked
            .size
            .takeUnless { isFinding },
        mentions
            .mentions
            .unlinked
            .size
            .takeUnless { isFinding }
    )
}

@Preview
@Composable
private fun ConnectionsSectionPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp)) {
            ConnectionsSection(
                tab = ConnectionsTab.LINKED,
                onTabSelect = {},
                isOpen = true,
                onToggle = {},
                related = RelatedNotesState(isIndexReady = true),
                mentions = NoteMentionsState(),
                onRelatedIntent = {},
                onMentionsIntent = {},
                actions = ConnectionActions(
                    onOpen = {},
                    onReveal = { _, _ -> },
                    onOpenBeside = {},
                    onInsertLink = {}
                )
            )
        }
    }
}
