package app.notekea.templates.domain

import app.notekea.core.notes.domain.VaultSettingsReader
import app.notekea.core.notes.domain.templates.TemplateSettings

/** The vault's Templates settings (`.obsidian/templates.json`), or Notekea's defaults when it has none. */
class GetTemplateSettingsUseCase(private val vaultSettingsReader: VaultSettingsReader) {
    suspend operator fun invoke(vaultPath: String): TemplateSettings =
        vaultSettingsReader.templates(
            vaultPath = vaultPath
        ) ?: TemplateSettings.DEFAULT
}
