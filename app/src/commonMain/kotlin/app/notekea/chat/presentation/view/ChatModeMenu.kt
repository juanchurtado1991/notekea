package app.notekea.chat.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.viewmodel.nameOf
import app.notekea.core.llm.domain.ModelTier
import app.notekea.core.presentation.designsystem.components.menus.NotekeaContextMenuArea
import app.notekea.core.presentation.designsystem.components.menus.NotekeaContextMenuState
import app.notekea.core.presentation.designsystem.components.menus.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.chat_mode_agent
import app.notekea.resources.chat_mode_agent_hint
import app.notekea.resources.chat_mode_auto
import app.notekea.resources.chat_mode_auto_hint
import app.notekea.resources.chat_mode_deep
import app.notekea.resources.chat_mode_deep_hint
import app.notekea.resources.chat_mode_description
import app.notekea.resources.chat_mode_fast
import app.notekea.resources.chat_mode_fast_hint
import app.notekea.resources.chat_mode_label
import app.notekea.resources.chat_model_large
import app.notekea.resources.chat_model_medium
import app.notekea.resources.chat_model_small
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The model in use and the chat's [mode] under the question field ("Medium · Auto"), as in Claude Code: a click opens
 * the [modes] the model offers, each with what it costs and gives, the chosen one checked.
 */
@Composable
internal fun ChatModeMenu(
    mode: ChatMode,
    modes: List<ChatMode>,
    onChoose: (ChatMode) -> Unit,
    tier: ModelTier?,
    modifier: Modifier = Modifier
) {
    val menuState = remember { NotekeaContextMenuState() }
    val modeName = stringResource(resource = nameOf(mode = mode))

    val label = tier?.let {
        stringResource(resource = Res.string.chat_mode_label, nameOf(tier = it), modeName)
    } ?: modeName

    val description = stringResource(resource = Res.string.chat_mode_description, label)

    val items = modes.map { choice ->
        NotekeaMenuItem(
            label = stringResource(resource = nameOf(mode = choice)),
            onClick = { onChoose(choice) },
            isChecked = choice == mode,
            supportingText = stringResource(resource = hintOf(mode = choice))
        )
    }

    NotekeaContextMenuArea(
        sections = listOf(items),
        modifier = modifier,
        state = menuState
    ) {
        Row(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.small)
                .clickable(role = Role.Button) { menuState.open(at = Offset.Zero) }
                .semantics { contentDescription = description }
                .padding(
                    horizontal = NotekeaDimensions.SpacingXs,
                    vertical = NotekeaDimensions.SpacingXs
                ),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = NotekeaIcons.Dropdown,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun nameOf(mode: ChatMode): StringResource = when (mode) {
    ChatMode.AUTO -> Res.string.chat_mode_auto
    ChatMode.FAST -> Res.string.chat_mode_fast
    ChatMode.DEEP -> Res.string.chat_mode_deep
    ChatMode.AGENT -> Res.string.chat_mode_agent
}

private fun hintOf(mode: ChatMode): StringResource = when (mode) {
    ChatMode.AUTO -> Res.string.chat_mode_auto_hint
    ChatMode.FAST -> Res.string.chat_mode_fast_hint
    ChatMode.DEEP -> Res.string.chat_mode_deep_hint
    ChatMode.AGENT -> Res.string.chat_mode_agent_hint
}

@Composable
private fun nameOf(tier: ModelTier): String {
    val name = when (tier) {
        ModelTier.SMALL -> Res.string.chat_model_small
        ModelTier.MEDIUM -> Res.string.chat_model_medium
        ModelTier.LARGE -> Res.string.chat_model_large
    }

    return stringResource(resource = name)
}

@Preview
@Composable
private fun ChatModeMenuPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) {
            ChatModeMenu(
                tier = ModelTier.MEDIUM,
                mode = ChatMode.AUTO,
                modes = ChatMode.entries,
                onChoose = {}
            )
        }
    }
}
