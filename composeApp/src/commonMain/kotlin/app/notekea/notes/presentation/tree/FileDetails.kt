package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaAttachmentImage
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.file_panel_image_size
import app.notekea.resources.file_panel_open
import app.notekea.resources.file_panel_unreadable
import org.jetbrains.compose.resources.stringResource

private const val CONTENT_MAX_WIDTH_DP = 640
private const val PREVIEW_HEIGHT = 120
private const val PREVIEW_PANEL_HEIGHT_DP = 420
private const val PREVIEW_PANEL_WIDTH_DP = 520
private const val PREVIEW_WIDTH = 240

/**
 * A file of the vault that is not a note, selected in the tree (`ROADMAP.md` 1.8): its name and folder, then the
 * picture itself (with its size in pixels), or why an image cannot be shown; below, Open with its app (the way to see a
 * PDF or any other file) and Show in the file manager.
 */
@Composable
internal fun FileDetails(
    preview: FilePreview,
    platformConventions: PlatformConventions,
    onIntent: (VaultTreeIntent) -> Unit
) {
    val path = preview.file.path
    val open = { onIntent(VaultTreeIntent.OnFileOpen(path = path)) }
    Column(
        modifier = Modifier
            .verticalScroll(state = rememberScrollState())
            .widthIn(max = CONTENT_MAX_WIDTH_DP.dp)
            .padding(
                horizontal = NotekeaDimensions.SpacingXxl,
                vertical = NotekeaDimensions.SpacingXxl + NotekeaDimensions.SpacingL
            ),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
            Text(text = path.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = detailLine(preview = preview),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val image = preview.image
        if (image != null) {
            NotekeaAttachmentImage(bitmap = image, contentDescription = path.name)
        } else if (preview.isUnreadable) {
            Text(text = stringResource(resource = Res.string.file_panel_unreadable))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            NotekeaButton(text = stringResource(resource = Res.string.file_panel_open), onClick = open)
            NotekeaButton(
                text = stringResource(resource = revealLabel(operatingSystem = platformConventions.operatingSystem)),
                onClick = { onIntent(VaultTreeIntent.OnRevealRequest(path = path)) },
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }
}

/** The folder the file is in, and the size of a picture: `attachments · Image · 480 × 240`. */
@Composable
private fun detailLine(
    preview: FilePreview
): String {
    val folder = preview.file.path.parent?.takeUnless { it.isRoot }?.value
    val size = preview.image?.let { stringResource(Res.string.file_panel_image_size, it.width, it.height) }
    return listOfNotNull(folder, size).joinToString(separator = DETAIL_SEPARATOR)
}

private const val DETAIL_SEPARATOR = " · "

@Preview
@Composable
private fun FileDetailsPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_PANEL_WIDTH_DP.dp).height(height = PREVIEW_PANEL_HEIGHT_DP.dp)) {
            FileDetails(
                preview = FilePreview(
                    file = NoteFile(
                        path = RelativePath(value = "attachments/Arepas plate.png"),
                        size = null,
                        modifiedAt = null
                    ),
                    kind = AttachmentKind.IMAGE,
                    image = ImageBitmap(
                        width = PREVIEW_WIDTH,
                        height = PREVIEW_HEIGHT,
                        config = ImageBitmapConfig.Argb8888
                    )
                ),
                platformConventions = PreviewPlatformConventions.linux,
                onIntent = {}
            )
        }
    }
}
