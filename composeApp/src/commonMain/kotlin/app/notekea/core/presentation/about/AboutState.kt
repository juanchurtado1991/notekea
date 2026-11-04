package app.notekea.core.presentation.about

/** What About shows besides its fixed texts: whether Copy diagnostics put them on the clipboard, or could not. */
data class AboutState(
    val isDiagnosticsCopied: Boolean = false,
    val hasCopyFailed: Boolean = false
)
