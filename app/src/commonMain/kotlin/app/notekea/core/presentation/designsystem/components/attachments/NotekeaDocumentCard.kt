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
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .focusBorder(
                color = colors.primary,
                shape = MaterialTheme.shapes.small
            )
            .clip(shape = MaterialTheme.shapes.small)
            .clickable(
                onClickLabel = hint,
                role = Role.Button,
                onClick = onOpen
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
            Image(
                bitmap = page,
                contentDescription = null,
                modifier = Modifier
                    .width(width = PAGE_WIDTH_DP.dp)
                    .aspectRatio(ratio = PAGE_RATIO)
                    .clip(shape = MaterialTheme.shapes.extraSmall)
                    .background(color = colors.surfaceVariant),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
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
