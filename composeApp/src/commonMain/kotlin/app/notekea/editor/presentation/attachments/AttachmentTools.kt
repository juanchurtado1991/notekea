package app.notekea.editor.presentation.attachments

import app.notekea.core.domain.AppDispatchers
import app.notekea.editor.domain.AttachFileUseCase
import app.notekea.editor.domain.AttachmentPicker
import app.notekea.core.notes.domain.AttachmentStore
import app.notekea.editor.domain.ClipboardReader
import app.notekea.core.notes.domain.FileOpener
import app.notekea.core.notes.domain.VaultSettingsReader

/** What the editor needs for a note's attachments (`ROADMAP.md` 1.8), given together so its constructor stays short. */
class AttachmentTools(
    val attachmentStore: AttachmentStore,
    val vaultSettingsReader: VaultSettingsReader,
    val attachFile: AttachFileUseCase,
    val fileOpener: FileOpener,
    val attachmentPicker: AttachmentPicker,
    val clipboardReader: ClipboardReader,
    val dispatchers: AppDispatchers
)
