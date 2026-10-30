package app.notekea.notes.presentation.tree

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.AttachmentStore
import app.notekea.notes.domain.NoteFile
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

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
        val image = bytes?.let {
            withContext(context = dispatchers.default) {
                try {
                    it.decodeToImageBitmap()
                } catch (_: IllegalArgumentException) {
                    // Not a picture Skia can read (a broken or renamed file): it is offered to open instead.
                    null
                }
            }
        }
        return FilePreview(file = file, kind = kind, image = image, isUnreadable = image == null)
    }
}
