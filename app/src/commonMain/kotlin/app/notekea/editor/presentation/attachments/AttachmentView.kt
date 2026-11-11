package app.notekea.editor.presentation.attachments

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.domain.Embed
import app.notekea.editor.domain.EmbedProblem

/**
 * One file embedded in the open note: [path] found (null when missing), an image's decoded [image], a video's place
 * on the disk ([location]) to play, or an embedded note's [noteText] with its own embeds ([nested]) or the [problem]
 * that keeps it from showing.
 */
data class AttachmentView(
    val embed: Embed,
    val kind: AttachmentKind,
    val path: RelativePath?,
    val image: ImageBitmap? = null,
    val isUnreadable: Boolean = false,
    val noteText: String? = null,
    val nested: List<AttachmentView> = emptyList(),
    val problem: EmbedProblem? = null,
    val location: String? = null
) {
    /** Shown as a picture: an image that was found, read and drawn. */
    val isPicture: Boolean get() = kind == AttachmentKind.IMAGE && image != null
}
