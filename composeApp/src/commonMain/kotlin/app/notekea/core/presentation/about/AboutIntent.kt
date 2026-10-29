package app.notekea.core.presentation.about

/** What a person does in About. */
sealed interface AboutIntent {
    data object OnCopyDiagnostics : AboutIntent
}
