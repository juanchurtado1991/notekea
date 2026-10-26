package app.notekea.notes.presentation.editor

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.RelativePath

/**
 * One file embedded in the open note, ready to show (`ROADMAP.md` 1.8): [path] is the file it found, null when it is
 * missing (shown with Locate…); [image] is the decoded picture of an image, null while it loads or when it cannot be
 * drawn, in which case it shows as a file chip like any other file. An embedded note ([AttachmentKind.NOTE]) carries
 * the [noteText] it shows: the whole note or the section its embed names.
 */
data class AttachmentView(
    val embed: Embed,
    val kind: AttachmentKind,
    val path: RelativePath?,
    val image: ImageBitmap? = null,
    val isUnreadable: Boolean = false,
    val noteText: String? = null
) {
    /** Shown as a picture: an image that was found, read and drawn. */
    val isPicture: Boolean get() = kind == AttachmentKind.IMAGE && image != null
}
