package app.notekea.editor.presentation.attachments

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.domain.Embed
import app.notekea.editor.domain.EmbedProblem

/**
 * One file embedded in the open note, ready to show: [path] is the file it found, null when it is missing; [image] is
 * an image's decoded picture, null while it loads or cannot be drawn. An embedded note ([AttachmentKind.NOTE]) carries
 * the [noteText] it shows and what that text embeds ([nested]), or the [problem] that keeps it from showing.
 */
data class AttachmentView(
    val embed: Embed,
    val kind: AttachmentKind,
    val path: RelativePath?,
    val image: ImageBitmap? = null,
    val isUnreadable: Boolean = false,
    val noteText: String? = null,
    val nested: List<AttachmentView> = emptyList(),
    val problem: EmbedProblem? = null
) {
    /** Shown as a picture: an image that was found, read and drawn. */
    val isPicture: Boolean get() = kind == AttachmentKind.IMAGE && image != null
}
