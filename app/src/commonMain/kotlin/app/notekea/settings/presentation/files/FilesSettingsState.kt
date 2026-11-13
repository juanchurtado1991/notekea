package app.notekea.settings.presentation.files

/**
 * Settings → Files for the open vault ([hasVault]): where its attachments, daily notes (and their date format) and
 * templates go ("" is the vault's top folder), chosen among the vault's [folders] and the [formats] offered, and
 * whether the last one could not be saved ([hasFailed]).
 */
data class FilesSettingsState(
    val hasVault: Boolean = false,
    val attachments: String = "",
    val dailyFolder: String = "",
    val dailyFormat: String = "",
    val templatesFolder: String = "",
    val hasFailed: Boolean = false,
    val folders: List<String> = emptyList(),
    val formats: List<DateFormatChoice> = emptyList()
) {
    /** The folders a setting can be given: the vault's, and [current] when it is not one of them yet. */
    fun foldersWith(current: String): List<String> = (folders + current)
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()
}
