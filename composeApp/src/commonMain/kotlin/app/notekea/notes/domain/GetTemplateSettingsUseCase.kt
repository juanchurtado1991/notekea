package app.notekea.notes.domain

/** The vault's Templates settings (`.obsidian/templates.json`), or Notekea's defaults when it has none. */
class GetTemplateSettingsUseCase(private val vaultSettingsReader: VaultSettingsReader) {
    suspend operator fun invoke(
        vaultPath: String
    ): TemplateSettings = vaultSettingsReader.templates(vaultPath = vaultPath) ?: TemplateSettings.DEFAULT
}
