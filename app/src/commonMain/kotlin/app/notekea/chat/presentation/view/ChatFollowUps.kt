package app.notekea.chat.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.chat.presentation.model.FollowUps
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_follow_up_about
import app.notekea.resources.chat_follow_up_title

private const val HOVER_ALPHA = 0.06f
private const val ARROW = "→"

/**
 * Under the chat's last answer, what to ask next ([FollowUps]): a quiet line each, asked with a click ([onAsk]).
 * A heading that is itself a question is asked as it is, any other as "Tell me more about …".
 */
@Composable
internal fun ChatFollowUps(
    entry: ChatEntry,
    onAsk: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val topics = remember(entry.sources, entry.answer, entry.outcome) {
        FollowUps.of(entry = entry)
    }

    if (topics.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Text(
            text = replyString(resource = Res.string.chat_follow_up_title),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        topics.forEach { topic ->
            val isQuestion = topic.endsWith(suffix = "?")
            val about = replyString(Res.string.chat_follow_up_about, topic)
            val question = if (isQuestion) topic else about

            FollowUpLine(
                text = question,
                onClick = { onAsk(question) }
            )
        }
    }
}

@Composable
private fun FollowUpLine(
    text: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.extraSmall
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val fill = if (isHovered) colors.primary.copy(alpha = HOVER_ALPHA) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = fill)
            .focusBorder(
                color = colors.primary,
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                horizontal = NotekeaDimensions.SpacingXs,
                vertical = NotekeaDimensions.SpacingXs
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = ARROW,
            style = MaterialTheme.typography.bodySmall,
            color = colors.primary
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun ChatFollowUpsPreview() {
    NotekeaPreviewThemes {
        ChatFollowUps(
            entry = ChatPreviewData.answered,
            onAsk = {}
        )
    }
}
