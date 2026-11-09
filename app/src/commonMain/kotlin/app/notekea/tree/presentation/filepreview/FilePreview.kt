package app.notekea.tree.presentation.filepreview

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.NoteFile

/**
 * A file that is not a note, selected in the tree and shown beside it: its [image] when it is a
 * picture Notekea can draw, null for other files and for an image that cannot be read ([isUnreadable]).
 */
data class FilePreview(
    val file: NoteFile,
    val kind: AttachmentKind,
    val image: ImageBitmap? = null,
    val isUnreadable: Boolean = false
)
