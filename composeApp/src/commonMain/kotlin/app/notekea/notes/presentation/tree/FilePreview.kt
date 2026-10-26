package app.notekea.notes.presentation.tree

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.NoteFile

/**
 * A file that is not a note, selected in the tree and shown beside it (`ROADMAP.md` 1.8): its [image] when it is a
 * picture Notekea can draw, null for other files and for an image that cannot be read ([isUnreadable]).
 */
data class FilePreview(
    val file: NoteFile,
    val kind: AttachmentKind,
    val image: ImageBitmap? = null,
    val isUnreadable: Boolean = false
)
