package app.notekea.settings.presentation.files

/** What a person types in Settings → Files; each is saved a moment after the last key. */
sealed interface FilesSettingsIntent {
    /** The page shows: what the vault says now is read again, since another program may have changed it. */
    data object OnShown : FilesSettingsIntent

    data class OnAttachmentsChange(val folder: String) : FilesSettingsIntent

    data class OnDailyFolderChange(val folder: String) : FilesSettingsIntent

    data class OnDailyFormatChange(val format: String) : FilesSettingsIntent

    data class OnTemplatesFolderChange(val folder: String) : FilesSettingsIntent
}
