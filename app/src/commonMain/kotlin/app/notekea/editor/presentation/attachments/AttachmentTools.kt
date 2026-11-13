package app.notekea.editor.presentation.attachments

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.editor.domain.AttachFileUseCase
import app.notekea.editor.domain.AttachmentPicker
import app.notekea.editor.domain.ClipboardReader
import app.notekea.editor.domain.FilePosters
import app.notekea.editor.domain.VideoCompressor
import app.notekea.editor.domain.YouTubeDetails

/** What the editor needs for a note's attachments, given together so its constructor stays short. */
class AttachmentTools(
    val attachmentStore: AttachmentStore,
    val vaultSettingsReader: VaultSettingsReader,
    val attachFile: AttachFileUseCase,
    val fileOpener: FileOpener,
    val attachmentPicker: AttachmentPicker,
    val clipboardReader: ClipboardReader,
    val youTubeDetails: YouTubeDetails,
    val videoCompressor: VideoCompressor,
    val dispatchers: AppDispatchers,
    val filePosters: FilePosters = FilePosters { _, _ -> null }
)
