package app.notekea.ai.presentation

sealed interface ModelDownloadIntent {
    /** The status bar's entry was clicked: Settings opens at AI model, where the download is run. */
    data object OnOpen : ModelDownloadIntent
}
