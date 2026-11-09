package app.notekea.tree.domain

import app.notekea.core.notes.domain.DailyNoteSettings
import app.notekea.core.notes.domain.VaultSettingsReader

/** The vault's Daily notes settings (`.obsidian/daily-notes.json`), or Notekea's defaults when it has none. */
class GetDailyNoteSettingsUseCase(private val vaultSettingsReader: VaultSettingsReader) {
    suspend operator fun invoke(
        vaultPath: String
    ): DailyNoteSettings = vaultSettingsReader.dailyNotes(vaultPath = vaultPath) ?: DailyNoteSettings.DEFAULT
}
