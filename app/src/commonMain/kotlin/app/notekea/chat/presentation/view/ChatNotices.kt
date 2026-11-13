package app.notekea.chat.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.chat.presentation.model.ModelStatus
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.chat_empty_message
import app.notekea.resources.chat_empty_title
import app.notekea.resources.chat_memory_message
import app.notekea.resources.chat_memory_title
import app.notekea.resources.chat_model_action
import app.notekea.resources.chat_model_failed_message
import app.notekea.resources.chat_model_failed_title
import app.notekea.resources.chat_no_model_message
import app.notekea.resources.chat_no_model_title
import org.jetbrains.compose.resources.stringResource

private const val BADGE_DP = 44
private const val BADGE_ALPHA = 0.14f

/**
 * What the chat shows instead of a conversation: why it cannot answer ([status] other than ready), with Settings →
 * AI model ([onModelSettingsOpen]), or the invitation to ask when it can and nothing was asked yet.
 */
@Composable
internal fun ChatNotice(
    status: ModelStatus,
    onModelSettingsOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, message) = when (status) {
        ModelStatus.NO_MODEL ->
            Res.string.chat_no_model_title to Res.string.chat_no_model_message

        ModelStatus.NOT_ENOUGH_MEMORY ->
            Res.string.chat_memory_title to Res.string.chat_memory_message

        ModelStatus.FAILED ->
            Res.string.chat_model_failed_title to Res.string.chat_model_failed_message

        else -> Res.string.chat_empty_title to Res.string.chat_empty_message
    }

    val canAnswer = status == ModelStatus.READY || status == ModelStatus.CHECKING

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = NotekeaDimensions.SpacingL),
        verticalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingM,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Badge(icon = if (canAnswer) NotekeaIcons.ChatAnswer else NotekeaIcons.ChatModel)

        Text(
            text = stringResource(resource = title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(resource = message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (!canAnswer) {
            NotekeaButton(
                text = stringResource(resource = Res.string.chat_model_action),
                onClick = onModelSettingsOpen
            )
        }
    }
}

/** The notice's icon on a soft disc of the theme's color. */
@Composable
private fun Badge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(size = BADGE_DP.dp)
            .background(
                color = MaterialTheme
                    .colorScheme
                    .primary
                    .copy(alpha = BADGE_ALPHA),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
private fun ChatNoticePreview() {
    NotekeaPreviewThemes {
        Column {
            ChatNotice(
                status = ModelStatus.READY,
                onModelSettingsOpen = {},
                modifier = Modifier.size(300.dp, 220.dp)
            )

            ChatNotice(
                status = ModelStatus.NO_MODEL,
                onModelSettingsOpen = {},
                modifier = Modifier.size(300.dp, 260.dp)
            )
        }
    }
}
