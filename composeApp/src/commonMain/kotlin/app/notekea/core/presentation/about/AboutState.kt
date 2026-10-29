package app.notekea.core.presentation.about

/** What About shows besides its fixed texts: whether Copy diagnostics has put them on the clipboard. */
data class AboutState(
    val isDiagnosticsCopied: Boolean = false
)
