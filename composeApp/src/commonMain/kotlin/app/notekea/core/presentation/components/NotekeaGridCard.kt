package app.notekea.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val ASPECT_RATIO = 4f / 3f
private const val ICON_SIZE_DP = 32
private const val PREVIEW_NOTE = "Tomato plan"
private const val PREVIEW_WIDTH_DP = 180
private const val SCRIM_ALPHA = 0.7f
private const val SCRIM_START = 0.45f

/**
 * A card of a grid of files (`ROADMAP.md` 1.11: a folder's notes): a rectangle with the [label] at its bottom. With an
 * [image] (the picture a note shows, or an image itself) the picture fills the card, cropped, under a dark shade that
 * keeps the label readable; without one the card shows [icon] on the surface. A click goes to [onClick]; the card draws
 * the focus ring when it has the keyboard.
 */
@Composable
fun NotekeaGridCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: ImageBitmap? = null
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio = ASPECT_RATIO)
            .clip(shape = shape)
            .border(width = NotekeaDimensions.BorderWidth, color = colors.outlineVariant, shape = shape)
            .focusBorder(color = colors.primary, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .background(color = colors.surfaceVariant)
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.verticalGradient(
                        SCRIM_START to Color.Transparent,
                        1f to colors.scrim.copy(alpha = SCRIM_ALPHA)
                    )
                )
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.align(alignment = Alignment.Center).size(size = ICON_SIZE_DP.dp),
                tint = colors.onSurfaceVariant
            )
        }
        CardLabel(label = label, icon = icon, isOnImage = image != null)
    }
}

@Composable
private fun BoxScope.CardLabel(
    label: String,
    icon: ImageVector,
    isOnImage: Boolean
) {
    val color = if (isOnImage) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .align(alignment = Alignment.BottomStart)
            .fillMaxWidth()
            .padding(all = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isOnImage) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = color
            )
            Spacer(modifier = Modifier.width(width = NotekeaDimensions.SpacingXs))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun NotekeaGridCardPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaGridCard(label = PREVIEW_NOTE, icon = NotekeaIcons.Note, onClick = {})
        }
    }
}
