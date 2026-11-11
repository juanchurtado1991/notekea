package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val THUMBNAIL_WIDTH_DP = 120
private const val VIDEO_RATIO = 16f / 9f
private const val PLAY_DP = 28
private const val PREVIEW_TITLE = "Cómo hacer arepas"
private const val PREVIEW_SUBTITLE = "YouTube · 3:10"
private const val PREVIEW_PLAY = "Play"

/**
 * A video in a note before it plays (the developer's design B, 2026-11-11): a small picture of it ([thumbnail], or the
 * theme's dark surface with a play mark), its [title] and, smaller, where it is from ([subtitle]); a click plays it
 * ([onPlay]), in place or in a window of its own. Nothing is loaded to play until then.
 */
@Composable
fun NotekeaVideoCard(
    title: String,
    subtitle: String,
    playLabel: String,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: ImageBitmap? = null
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .focusBorder(
                color = colors.primary,
                shape = MaterialTheme.shapes.small
            )
            .clip(shape = MaterialTheme.shapes.small)
            .clickable(
                onClickLabel = playLabel,
                role = Role.Button,
                onClick = onPlay
            ),
        shape = MaterialTheme.shapes.small,
        color = colors.surface,
        border = BorderStroke(
            width = NotekeaDimensions.BorderWidth,
            color = colors.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Thumbnail(thumbnail = thumbnail)

            Column(
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

// The video's picture, or a dark frame, with a play mark over it.
@Composable
private fun Thumbnail(thumbnail: ImageBitmap?) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .width(width = THUMBNAIL_WIDTH_DP.dp)
            .aspectRatio(ratio = VIDEO_RATIO)
            .clip(shape = MaterialTheme.shapes.extraSmall)
            .background(color = colors.scrim),
        contentAlignment = Alignment.Center
    ) {
        thumbnail?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .size(size = PLAY_DP.dp)
                .clip(shape = MaterialTheme.shapes.extraLarge)
                .background(color = colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = NotekeaIcons.Play,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = colors.onPrimary
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaVideoCardPreview() {
    NotekeaPreviewThemes {
        NotekeaVideoCard(
            title = PREVIEW_TITLE,
            subtitle = PREVIEW_SUBTITLE,
            playLabel = PREVIEW_PLAY,
            onPlay = {}
        )
    }
}
