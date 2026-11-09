package app.notekea.connections.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.components.NotekeaLazyScrollArea
import app.notekea.core.presentation.designsystem.components.NotekeaPassageRow
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.connections.domain.NoteMention
import app.notekea.connections.domain.NoteMentions
import app.notekea.core.notes.domain.RelativePath
import app.notekea.connections.presentation.NoteMentionsIntent
import app.notekea.connections.presentation.NoteMentionsState
import app.notekea.resources.Res
import app.notekea.resources.mentions_count
import app.notekea.resources.mentions_failed
import app.notekea.resources.mentions_link
import app.notekea.resources.mentions_link_failed
import app.notekea.resources.mentions_linked_empty
import app.notekea.resources.mentions_unlinked_empty
import app.notekea.resources.related_open_beside
import app.notekea.resources.tree_retry
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val PILL_HEIGHT_DP = 22
private const val PILL_ICON_DP = 12
private const val PREVIEW_HEIGHT_DP = 160
private const val PILL_GAP_DP = 4

/**
 * Connections' Linked or Unlinked tab (`ROADMAP.md` 2.5, design A): a row per note that names the open one, with the
 * line where it does and the mention highlighted (and how many times, when more than once); a click opens that note
 * there. A linked row offers Open to the side; an unlinked one a Link button, the only way it becomes a link. Bars stand
 * in for the rows the first time they are found, and a search that failed says so, with Try again.
 */
@Composable
fun MentionList(
    state: NoteMentionsState,
    isLinked: Boolean,
    onIntent: (NoteMentionsIntent) -> Unit,
    actions: ConnectionActions
) {
    val mentions = if (isLinked) state.mentions.linked else state.mentions.unlinked
    val listState = rememberLazyListState()
    NotekeaLazyScrollArea(state = listState) {
        LazyColumn(state = listState) {
            item(key = STATUS_ITEM, contentType = STATUS_ITEM) {
                MentionsStatus(state = state, isLinked = isLinked, isEmpty = mentions.isEmpty(), onIntent = onIntent)
            }
            items(items = mentions, key = { it.source.value }, contentType = { MENTION_ITEM }) { mention ->
                MentionRow(mention = mention, isLinked = isLinked, onIntent = onIntent, actions = actions)
            }
        }
    }
}

/** What is said above the rows: a Link that failed, and a search that failed, under way or that found nothing. */
@Composable
private fun MentionsStatus(
    state: NoteMentionsState,
    isLinked: Boolean,
    isEmpty: Boolean,
    onIntent: (NoteMentionsIntent) -> Unit
) {
    Column {
        state.linkFailed?.takeUnless { isLinked }?.let {
            ConnectionsMuted(text = stringResource(Res.string.mentions_link_failed, it.nameWithoutExtension))
        }
        when {
            state.searchFailed -> MentionsFailed(onRetry = { onIntent(NoteMentionsIntent.OnRetry) })

            isEmpty && state.isLoading -> ConnectionsSkeleton()

            isEmpty -> {
                val name = state.note?.nameWithoutExtension.orEmpty()
                ConnectionsMuted(
                    text = if (isLinked) {
                        stringResource(resource = Res.string.mentions_linked_empty)
                    } else {
                        stringResource(Res.string.mentions_unlinked_empty, name)
                    }
                )
            }
        }
    }
}

/** The search failed: it says so and offers to try again, never that nothing was found. */
@Composable
private fun MentionsFailed(
    onRetry: () -> Unit
) {
    ConnectionsMuted(text = stringResource(resource = Res.string.mentions_failed))
    NotekeaButton(
        text = stringResource(resource = Res.string.tree_retry),
        onClick = onRetry,
        modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingS),
        style = NotekeaButtonStyle.TEXT
    )
}

@Composable
private fun MentionRow(
    mention: NoteMention,
    isLinked: Boolean,
    onIntent: (NoteMentionsIntent) -> Unit,
    actions: ConnectionActions
) {
    val source = mention.source
    val onReveal = { actions.onReveal(source, mention.range) }
    val onBeside = { actions.onOpenBeside(source) }
    val onLink = { onIntent(NoteMentionsIntent.OnLink(mention = mention)) }
    val beside = stringResource(Res.string.related_open_beside)
    val rowAction = if (isLinked) {
        connectionAction(label = beside, onAction = onBeside)
    } else {
        connectionAction(label = stringResource(resource = Res.string.mentions_link), onAction = onLink)
    }
    ConnectionMenu(onOpen = onReveal, onOpenBeside = onBeside) {
        NotekeaPassageRow(
            title = source.nameWithoutExtension,
            subtitle = subtitleOf(mention = mention),
            text = mention.passage,
            highlights = listOf(mention.highlight),
            icon = NotekeaIcons.Note,
            onClick = onReveal,
            showsActions = !isLinked,
            accessibilityActions = listOf(rowAction)
        ) {
            if (isLinked) {
                NotekeaIconButton(icon = NotekeaIcons.SidePanel, label = beside, onClick = onBeside)
            } else {
                LinkButton(onClick = onLink)
            }
        }
    }
}

/** The note's folder and, when it names the open one more than once, how many times. */
@Composable
private fun subtitleOf(
    mention: NoteMention
): String {
    val times = if (mention.count > 1) {
        pluralStringResource(Res.plurals.mentions_count, mention.count, mention.count)
    } else {
        ""
    }
    return listOf(folderOf(path = mention.source), times).filter { it.isNotEmpty() }
        .joinToString(separator = SUBTITLE_SEPARATOR)
}

/** Unlinked's Link: small, in the accent color, the row's one action. */
@Composable
private fun LinkButton(
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = Modifier
            .height(height = PILL_HEIGHT_DP.dp)
            .clip(shape = shape)
            .focusBorder(color = colors.onPrimary, shape = shape)
            .background(color = colors.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = NotekeaIcons.NoteLink,
            contentDescription = null,
            modifier = Modifier.size(size = PILL_ICON_DP.dp),
            tint = colors.onPrimary
        )
        Spacer(modifier = Modifier.width(width = PILL_GAP_DP.dp))
        Text(
            text = stringResource(resource = Res.string.mentions_link),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onPrimary
        )
    }
}

private const val MENTION_ITEM = "mention"
private const val STATUS_ITEM = "status"
private const val SUBTITLE_SEPARATOR = " · "

@Preview
@Composable
private fun MentionListPreview() {
    val passage = "We found arepas at a small place by the river."
    NotekeaPreviewThemes {
        Box(modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp)) {
            MentionList(
                state = NoteMentionsState(
                    note = RelativePath(value = "Recipes/Arepas.md"),
                    mentions = NoteMentions(
                        linked = emptyList(),
                        unlinked = listOf(
                            NoteMention(
                                source = RelativePath(value = "Travel/Lisbon trip.md"),
                                range = 9..14,
                                count = 2,
                                passage = passage,
                                highlight = 9..14
                            )
                        )
                    )
                ),
                isLinked = false,
                onIntent = {},
                actions = ConnectionActions(onOpen = {}, onReveal = { _, _ -> }, onOpenBeside = {}, onInsertLink = {})
            )
        }
    }
}
