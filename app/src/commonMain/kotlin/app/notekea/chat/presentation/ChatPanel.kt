package app.notekea.chat.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.ChatScope
import app.notekea.core.presentation.designsystem.components.NotekeaContextMenuArea
import app.notekea.core.presentation.designsystem.components.NotekeaContextMenuState
import app.notekea.core.presentation.designsystem.components.NotekeaMenuButton
import app.notekea.core.presentation.designsystem.components.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.NotekeaPromptField
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.chat_clear
import app.notekea.resources.chat_index_wait
import app.notekea.resources.chat_more
import app.notekea.resources.chat_placeholder
import app.notekea.resources.chat_scope_folder
import app.notekea.resources.chat_scope_label
import app.notekea.resources.chat_scope_note
import app.notekea.resources.chat_scope_vault
import app.notekea.resources.chat_send
import app.notekea.resources.chat_stop
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 320
private const val PREVIEW_HEIGHT_DP = 560

/**
 * The side panel's Chat (3.3, design A): the scope and the history's menu above the conversation, the question field
 * below it, which [inputFocus] gives the keyboard to (`Mod+L`). Without a model it offers Settings → AI model.
 */
@Composable
fun ChatPanel(
    state: ChatState,
    onIntent: (ChatIntent) -> Unit,
    inputFocus: FocusRequester,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onIntent(ChatIntent.OnShown) }

    val isReady = state.model == ModelStatus.READY

    Column(modifier = modifier.fillMaxSize()) {
        if (isReady) {
            ChatHeader(
                state = state,
                onIntent = onIntent
            )
        }

        Box(modifier = Modifier.weight(weight = 1f).fillMaxWidth()) {
            if (isReady && state.entries.isNotEmpty()) {
                ChatMessages(
                    entries = state.entries,
                    isSmall = state.isSmall,
                    onIntent = onIntent
                )
            } else {
                ChatNotice(
                    status = state.model,
                    onModelSettingsOpen = { onIntent(ChatIntent.OnModelSettingsOpen) }
                )
            }
        }

        if (isReady) {
            ChatInput(
                state = state,
                onIntent = onIntent,
                inputFocus = inputFocus
            )
        }
    }
}

@Composable
private fun ChatHeader(
    state: ChatState,
    onIntent: (ChatIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingXs
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScopePicker(
            state = state,
            onSelect = { onIntent(ChatIntent.OnScopeSelect(scope = it)) },
            modifier = Modifier.weight(weight = 1f)
        )

        NotekeaMenuButton(
            icon = NotekeaIcons.More,
            label = stringResource(resource = Res.string.chat_more),
            sections = listOf(
                listOf(
                    NotekeaMenuItem(
                        label = stringResource(resource = Res.string.chat_clear),
                        onClick = { onIntent(ChatIntent.OnClearHistory) },
                        icon = NotekeaIcons.ChatClear,
                        isDestructive = true,
                        enabled = state.entries.isNotEmpty()
                    )
                )
            ),
            takesFocus = false
        )
    }
}

/** "Ask: Whole vault ▾": the notes a question is asked of; the open note and its folder when there is one. */
@Composable
private fun ScopePicker(
    state: ChatState,
    onSelect: (ChatScope) -> Unit,
    modifier: Modifier = Modifier
) {
    val menu = remember { NotekeaContextMenuState() }

    val below = with(LocalDensity.current) {
        Offset(
            x = 0f,
            y = NotekeaDimensions.CompactControlSize.toPx()
        )
    }

    val note = state.noteName?.let { stringResource(Res.string.chat_scope_note, it) }
    val folder = state.folderName?.let { stringResource(Res.string.chat_scope_folder, it) }
    val vault = stringResource(resource = Res.string.chat_scope_vault)

    val choices = listOfNotNull(
        ChatScope.VAULT to vault,
        note?.let {
            ChatScope.NOTE to it
        },
        folder?.let {
            ChatScope.FOLDER to it
        }
    )

    val shown = choices.firstOrNull {
        it.first == state.scope
    }?.second ?: vault

    NotekeaContextMenuArea(
        sections = listOf(
            choices.map { (scope, label) ->
                NotekeaMenuItem(
                    label = label,
                    onClick = { onSelect(scope) },
                    isChecked = scope == state.scope
                )
            }
        ),
        modifier = modifier,
        state = menu,
        takesFocus = false
    ) {
        Row(
            modifier = Modifier
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .clickable(role = Role.DropdownList) { menu.open(at = below) }
                .padding(vertical = NotekeaDimensions.SpacingXs),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(resource = Res.string.chat_scope_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = shown,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = NotekeaIcons.Dropdown,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChatInput(
    state: ChatState,
    onIntent: (ChatIntent) -> Unit,
    inputFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) {
        if (!state.isIndexReady) {
            Text(
                text = stringResource(resource = Res.string.chat_index_wait),
                modifier = Modifier.padding(bottom = NotekeaDimensions.SpacingXs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        NotekeaPromptField(
            value = state.question,
            onValueChange = { onIntent(ChatIntent.OnQuestionChange(text = it)) },
            placeholder = stringResource(resource = Res.string.chat_placeholder),
            sendLabel = stringResource(resource = Res.string.chat_send),
            stopLabel = stringResource(resource = Res.string.chat_stop),
            isRunning = state.isAnswering,
            canSend = state.canSend,
            onSend = { onIntent(ChatIntent.OnSend) },
            onStop = { onIntent(ChatIntent.OnStop) },
            modifier = Modifier.focusRequester(focusRequester = inputFocus)
        )
    }
}

@Preview
@Composable
private fun ChatPanelPreview() {
    NotekeaPreviewThemes {
        Box(
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            ChatPanel(
                state = ChatPreviewData.state,
                onIntent = {},
                inputFocus = remember { FocusRequester() }
            )
        }
    }
}
