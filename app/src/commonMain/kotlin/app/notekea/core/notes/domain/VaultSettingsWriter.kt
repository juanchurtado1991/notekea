package app.notekea.core.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Settings → Files (5.2) writes the vault's settings where Obsidian keeps them, the files [VaultSettingsReader] reads,
 * so both apps agree (the developer's choice, 2026-11-11); every other setting in those files is kept as it was.
 */
interface VaultSettingsWriter {
    /** The `attachmentFolderPath` of `.obsidian/app.json`. */
    suspend fun setAttachmentFolder(
        vaultPath: String,
        folder: String
    ): Outcome<Unit, NotesError>

    /** The `folder` and `format` of `.obsidian/daily-notes.json`. */
    suspend fun setDailyNotes(
        vaultPath: String,
        folder: String,
        format: String
    ): Outcome<Unit, NotesError>

    /** The `folder` of `.obsidian/templates.json`. */
    suspend fun setTemplatesFolder(
        vaultPath: String,
        folder: String
    ): Outcome<Unit, NotesError>
}
