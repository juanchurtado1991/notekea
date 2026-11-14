package app.notekea.vault.presentation.welcome

/** What the user did on the welcome flow. */
sealed interface WelcomeIntent {
    data class OnCreateVaultClick(val suggestedName: String) : WelcomeIntent

    data object OnErrorDismiss : WelcomeIntent

    /** Import from a repository: its panel opens. */
    data object OnImportClick : WelcomeIntent

    /** Cancel on the import panel: an import under way stops, else the panel closes. */
    data object OnImportCancel : WelcomeIntent

    data object OnImportConfirm : WelcomeIntent

    data object OnImportLocationClick : WelcomeIntent

    data class OnImportNameChange(val name: String) : WelcomeIntent

    data class OnImportTokenChange(val token: String) : WelcomeIntent

    data class OnImportUrlChange(val url: String) : WelcomeIntent

    data class OnLocateVaultClick(val missingPath: String) : WelcomeIntent

    data object OnNewVaultCancel : WelcomeIntent

    data object OnNewVaultConfirm : WelcomeIntent

    data object OnNewVaultLocationClick : WelcomeIntent

    data class OnNewVaultNameChange(val name: String) : WelcomeIntent

    data object OnOpenFolderClick : WelcomeIntent

    /** Try the sample vault: a copy of the one Notekea ships, made in Documents and opened. */
    data object OnSampleVaultClick : WelcomeIntent

    data class OnRecentVaultClick(val path: String) : WelcomeIntent

    data class OnRemoveRecentVaultClick(val path: String) : WelcomeIntent

    data object OnSwitchVaultClick : WelcomeIntent
}
