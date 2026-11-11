package app.notekea.settings.presentation.files

/** What a person types in Settings → Files; each is saved a moment after the last key. */
sealed interface FilesSettingsIntent {
    data class OnAttachmentsChange(val folder: String) : FilesSettingsIntent

    data class OnDailyFolderChange(val folder: String) : FilesSettingsIntent

    data class OnDailyFormatChange(val format: String) : FilesSettingsIntent

    data class OnTemplatesFolderChange(val folder: String) : FilesSettingsIntent
}
