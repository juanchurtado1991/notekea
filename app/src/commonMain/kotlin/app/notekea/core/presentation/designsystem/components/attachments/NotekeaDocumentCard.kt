package app.notekea.core.presentation.designsystem.components.attachments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PAGE_WIDTH_DP = 72
private const val PAGE_RATIO = 3f / 4f
private const val PREVIEW_NAME = "menu.pdf"
private const val PREVIEW_HINT = "Opens in a tab"

/**
 * A document in a note (a PDF) with a picture of its first page ([page]) beside its [name] and, smaller, what a click
 * does ([hint]); a click opens it ([onOpen]). The developer's request of 2026-11-13: a PDF shows what it is.
 */
@Composable
fun NotekeaDocumentCard(
    name: String,
    hint: String,
    page: ImageBitmap,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotekeaMediaCard(
        title = name,
        subtitle = hint,
        clickLabel = hint,
        onClick = onOpen,
        modifier = modifier
    ) {
        Image(
            bitmap = page,
            contentDescription = null,
            modifier = Modifier
                .width(width = PAGE_WIDTH_DP.dp)
                .aspectRatio(ratio = PAGE_RATIO)
                .clip(shape = MaterialTheme.shapes.extraSmall)
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )
    }
}

@Preview
@Composable
private fun NotekeaDocumentCardPreview() {
    NotekeaPreviewThemes {
        NotekeaDocumentCard(
            name = PREVIEW_NAME,
            hint = PREVIEW_HINT,
            page = ImageBitmap(
                width = PREVIEW_PAGE_WIDTH,
                height = PREVIEW_PAGE_HEIGHT
            ),
            onOpen = {}
        )
    }
}

private const val PREVIEW_PAGE_WIDTH = 30
private const val PREVIEW_PAGE_HEIGHT = 40
