package app.notekea.tree.presentation.viewmodel

import app.notekea.core.domain.AppDispatchers
import app.notekea.tree.presentation.filepreview.FilePreview
import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.designsystem.images.decodedImage
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.core.notes.domain.NoteFile

/** Reads and decodes the files of the vault at [vaultPath] that the tree shows beside it ([FilePreview]). */
class TreeFilePreviews(
    private val vaultPath: String,
    private val attachmentStore: AttachmentStore,
    private val dispatchers: AppDispatchers
) {
    suspend fun of(
        file: NoteFile
    ): FilePreview {
        val kind = AttachmentKind.of(name = file.path.name)
        if (kind != AttachmentKind.IMAGE) return FilePreview(file = file, kind = kind)
        val bytes = (attachmentStore.read(vaultPath = vaultPath, path = file.path) as? Outcome.Success)?.value
        val image = bytes?.let { decodedImage(bytes = it, dispatcher = dispatchers.default) }
        return FilePreview(file = file, kind = kind, image = image, isUnreadable = image == null)
    }
}
