package app.notekea.vault.presentation.welcome

/** What the user did on the welcome flow. */
sealed interface WelcomeIntent {
    data class OnCreateVaultClick(val suggestedName: String) : WelcomeIntent

    data object OnErrorDismiss : WelcomeIntent

    data class OnLocateVaultClick(val missingPath: String) : WelcomeIntent

    data object OnNewVaultCancel : WelcomeIntent

    data object OnNewVaultConfirm : WelcomeIntent

    data object OnNewVaultLocationClick : WelcomeIntent

    data class OnNewVaultNameChange(val name: String) : WelcomeIntent

    data object OnOpenFolderClick : WelcomeIntent

    data class OnRecentVaultClick(val path: String) : WelcomeIntent

    data class OnRemoveRecentVaultClick(val path: String) : WelcomeIntent

    data object OnSwitchVaultClick : WelcomeIntent
}
