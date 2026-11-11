package app.notekea.settings.presentation.files

/**
 * Settings → Files for the open vault ([hasVault]): where its attachments, daily notes (and their date format) and
 * templates go, as typed ("" is the vault's top folder), and whether the last one could not be saved ([hasFailed]).
 */
data class FilesSettingsState(
    val hasVault: Boolean = false,
    val attachments: String = "",
    val dailyFolder: String = "",
    val dailyFormat: String = "",
    val templatesFolder: String = "",
    val hasFailed: Boolean = false
)
