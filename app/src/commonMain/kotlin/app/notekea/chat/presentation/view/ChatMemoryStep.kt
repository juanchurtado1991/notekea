package app.notekea.chat.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.chat_memory_forget

private const val MARK_DP = 12
private const val HOVER_ALPHA = 0.06f

/**
 * A line of the chat's memory a turn used, said as its step ([text], "From your memory: …"), with Forget shown on
 * hover so a wrong memory is put right where it is seen (`docs/agent-memory.md`, layer 5).
 */
@Composable
internal fun ChatMemoryStep(
    text: String,
    onForget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val fill = if (isHovered) colors.onSurface.copy(alpha = HOVER_ALPHA) else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.extraSmall)
            .background(color = fill)
            .hoverable(interactionSource = interaction)
            .padding(end = NotekeaDimensions.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(size = MARK_DP.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = NotekeaIcons.Bookmark,
                contentDescription = null,
                tint = colors.primary
            )
        }

        Text(
            text = text,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        if (isHovered) {
            NotekeaButton(
                text = replyString(resource = Res.string.chat_memory_forget),
                onClick = onForget,
                style = NotekeaButtonStyle.TEXT
            )
        }
    }
}

@Preview
@Composable
private fun ChatMemoryStepPreview() {
    NotekeaPreviewThemes {
        ChatMemoryStep(
            text = "From your memory: \"Laura\" is [[Laura Castro]]",
            onForget = {}
        )
    }
}
