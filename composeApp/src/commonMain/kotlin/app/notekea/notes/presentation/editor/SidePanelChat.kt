package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaComingSoon
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.chat_soon_badge
import app.notekea.resources.chat_soon_message
import app.notekea.resources.chat_soon_placeholder
import app.notekea.resources.chat_soon_title
import org.jetbrains.compose.resources.stringResource

private const val DASH_PX = 6f
private const val DIMMED_ALPHA = 0.5f
private const val PREVIEW_HEIGHT_DP = 480
private const val PREVIEW_WIDTH_DP = 320

/**
 * The side panel's Chat until the chat is built (`ROADMAP.md` 3.3): what it will do, marked "Coming soon", above the
 * question field it will have, drawn dimmed. That field is only a picture of one, never a text field: a disabled
 * text field left stale pixels on Linux once the panel went back to the inspector.
 */
@Composable
internal fun SidePanelChat(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        NotekeaComingSoon(
            icon = NotekeaIcons.ChatSoon,
            title = stringResource(resource = Res.string.chat_soon_title),
            message = stringResource(resource = Res.string.chat_soon_message),
            badge = stringResource(resource = Res.string.chat_soon_badge),
            modifier = Modifier.weight(weight = 1f).fillMaxWidth()
        )
        QuestionField()
    }
}

/**
 * The question field the chat will have, plainly not there yet: a dashed outline, as a placeholder is drawn, around
 * its dimmed placeholder, taking no input.
 */
@Composable
private fun QuestionField() {
    val outline = MaterialTheme.colorScheme.outline
    val radius = with(LocalDensity.current) { NotekeaDimensions.ControlRadius.toPx() }
    Text(
        text = stringResource(resource = Res.string.chat_soon_placeholder),
        modifier = Modifier
            .padding(all = NotekeaDimensions.SpacingS)
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(x = radius, y = radius),
                    style = Stroke(
                        width = NotekeaDimensions.BorderWidth.toPx(),
                        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(DASH_PX, DASH_PX))
                    )
                )
            }
            .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingM),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DIMMED_ALPHA)
    )
}

@Preview
@Composable
private fun SidePanelChatPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) { SidePanelChat() }
    }
}
