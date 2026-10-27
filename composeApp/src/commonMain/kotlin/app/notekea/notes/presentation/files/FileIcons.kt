package app.notekea.notes.presentation.files

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.notekea.core.presentation.theme.NotekeaColors
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.RelativePath

private const val EXTENSION_MARK = "."
private const val PDF_EXTENSION = "pdf"
private val PREVIEW_FILES = listOf("plate.png", "menu.pdf", "song.mp3", "list.txt")
private val MEDIA_EXTENSIONS = setOf("mp3", "wav", "ogg", "m4a", "flac", "mp4", "mov", "webm", "mkv")

/** The icon of a file that is not a note, by its kind: a picture, a sound or a video, or anything else (a PDF too). */
fun fileIconOf(
    path: RelativePath
): ImageVector = when {
    AttachmentKind.of(name = path.name) == AttachmentKind.IMAGE -> NotekeaIcons.ImageFile
    extensionOf(path = path) in MEDIA_EXTENSIONS -> NotekeaIcons.MediaFile
    else -> NotekeaIcons.OtherFile
}

/**
 * The color of a file's icon, so the kinds tell apart at a glance (pictures green, PDFs red, sounds and videos
 * violet); null for notes, folders and other files, which keep the muted icon color.
 */
@Composable
fun fileTintOf(
    path: RelativePath
): Color? = when {
    path.isMarkdown -> null
    AttachmentKind.of(name = path.name) == AttachmentKind.IMAGE -> NotekeaColors.fileImage
    extensionOf(path = path) == PDF_EXTENSION -> NotekeaColors.filePdf
    extensionOf(path = path) in MEDIA_EXTENSIONS -> NotekeaColors.fileMedia
    else -> null
}

@Preview
@Composable
private fun fileTintOfPreview() {
    NotekeaPreviewThemes {
        Row {
            PREVIEW_FILES.forEach { name ->
                val path = RelativePath(value = name)
                Icon(
                    imageVector = fileIconOf(path = path),
                    contentDescription = name,
                    tint = fileTintOf(path = path) ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun extensionOf(
    path: RelativePath
): String = path.name.substringAfterLast(delimiter = EXTENSION_MARK, missingDelimiterValue = "").lowercase()
