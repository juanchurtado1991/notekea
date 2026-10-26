package app.notekea.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_HEIGHT = 120
private const val PREVIEW_WIDTH = 480

/**
 * An image attached to a note, at its own size or the [width] the note asks for, never wider than the space it is
 * given, keeping its proportions, with the control's rounded corners. [contentDescription] is its alt text.
 */
@Composable
fun NotekeaAttachmentImage(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    width: Dp? = null
) {
    BoxWithConstraints(modifier = modifier) {
        val size = attachmentImageSize(
            imageWidth = bitmap.width,
            imageHeight = bitmap.height,
            maxWidth = maxWidth.value,
            requestedWidth = width?.value
        )
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier.size(
                width = size.width.dp,
                height = size.height.dp
            ).clip(shape = MaterialTheme.shapes.small),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview
@Composable
private fun NotekeaAttachmentImagePreview() {
    NotekeaPreviewThemes {
        NotekeaAttachmentImage(
            bitmap = ImageBitmap(width = PREVIEW_WIDTH, height = PREVIEW_HEIGHT, config = ImageBitmapConfig.Argb8888),
            contentDescription = null
        )
    }
}
