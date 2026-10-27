package app.notekea.notes.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.chat_soon_badge
import app.notekea.resources.chat_soon_message
import app.notekea.resources.chat_soon_placeholder
import app.notekea.resources.chat_soon_title
import org.jetbrains.compose.resources.stringResource

private const val BADGE_LETTER_SPACING_SP = 0.8
private const val MARK_DP = 48
private const val MARK_ICON_DP = 24
private const val PREVIEW_HEIGHT_DP = 480
private const val PREVIEW_WIDTH_DP = 320

/**
 * The side panel's Chat until the chat is built (`ROADMAP.md` 3.3): what it will do, marked "Coming soon", above the
 * question field it will have, shown disabled.
 */
@Composable
internal fun SidePanelChat(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(weight = 1f).fillMaxWidth().padding(all = NotekeaDimensions.SpacingXl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = NotekeaDimensions.SpacingS,
                alignment = Alignment.CenterVertically
            )
        ) {
            ChatMark()
            Text(
                text = stringResource(resource = Res.string.chat_soon_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(resource = Res.string.chat_soon_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            SoonBadge()
        }
        NotekeaTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS),
            placeholder = stringResource(resource = Res.string.chat_soon_placeholder),
            enabled = false
        )
    }
}

/** The chat's mark: its icon on the primary container. */
@Composable
private fun ChatMark() {
    Box(
        modifier = Modifier
            .size(size = MARK_DP.dp)
            .clip(shape = MaterialTheme.shapes.medium)
            .background(color = MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = NotekeaIcons.ChatSoon,
            contentDescription = null,
            modifier = Modifier.size(size = MARK_ICON_DP.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SoonBadge() {
    Text(
        text = stringResource(resource = Res.string.chat_soon_badge).uppercase(),
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = NotekeaDimensions.SpacingS, vertical = NotekeaDimensions.SpacingXs / 2),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = BADGE_LETTER_SPACING_SP.sp
        ),
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview
@Composable
private fun SidePanelChatPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) { SidePanelChat() }
    }
}
