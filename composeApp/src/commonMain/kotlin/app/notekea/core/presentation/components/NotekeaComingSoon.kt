package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val BADGE_LETTER_SPACING_SP = 0.8
private const val MARK_DP = 48
private const val MARK_ICON_DP = 24
private const val MESSAGE_MAX_WIDTH_DP = 360
private const val PREVIEW_BADGE = "Coming soon"
private const val PREVIEW_MESSAGE = "Choose the theme, the editor's font and more."
private const val PREVIEW_TITLE = "Settings"

/**
 * What a feature still to come will do, centered where it will be: its [icon] on the primary container, its [title],
 * a [message] saying what it will let the user do, and a [badge] ("Coming soon") in small capitals.
 */
@Composable
fun NotekeaComingSoon(
    icon: ImageVector,
    title: String,
    message: String,
    badge: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(all = NotekeaDimensions.SpacingXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.CenterVertically
        )
    ) {
        Mark(icon = icon)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            modifier = Modifier.widthIn(max = MESSAGE_MAX_WIDTH_DP.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Badge(text = badge)
    }
}

@Composable
private fun Mark(
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .size(size = MARK_DP.dp)
            .clip(shape = MaterialTheme.shapes.medium)
            .background(color = MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = MARK_ICON_DP.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun Badge(
    text: String
) {
    Text(
        text = text.uppercase(),
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
private fun NotekeaComingSoonPreview() {
    NotekeaPreviewThemes {
        NotekeaComingSoon(
            icon = NotekeaIcons.Settings,
            title = PREVIEW_TITLE,
            message = PREVIEW_MESSAGE,
            badge = PREVIEW_BADGE
        )
    }
}
