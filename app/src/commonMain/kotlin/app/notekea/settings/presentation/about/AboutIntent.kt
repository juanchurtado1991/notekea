package app.notekea.settings.presentation.about

/** What a person does in About. */
sealed interface AboutIntent {
    data object OnCopyDiagnostics : AboutIntent
}
