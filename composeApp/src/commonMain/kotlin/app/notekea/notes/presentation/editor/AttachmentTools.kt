package app.notekea.notes.presentation.editor

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.AttachFileUseCase
import app.notekea.notes.domain.AttachmentPicker
import app.notekea.notes.domain.AttachmentStore
import app.notekea.notes.domain.ClipboardReader
import app.notekea.notes.domain.FileOpener
import app.notekea.notes.domain.VaultSettingsReader

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
