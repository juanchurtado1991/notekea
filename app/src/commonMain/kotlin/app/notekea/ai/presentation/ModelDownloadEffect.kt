package app.notekea.ai.presentation

sealed interface ModelDownloadEffect {
    /** Opens Settings → AI model. */
    data object OpenSettings : ModelDownloadEffect
}
