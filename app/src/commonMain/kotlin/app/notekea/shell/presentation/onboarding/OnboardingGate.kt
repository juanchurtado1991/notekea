package app.notekea.shell.presentation.onboarding

/**
 * Whether the first-run welcome may show now. The developer's smoke of 2026-11-12: its card came up before the
 * program finished loading, over the splash and then over a vault still being read. It waits for the startup's
 * restore of the last vault and, with a vault open, for that vault's first scan.
 */
internal object OnboardingGate {
    fun isOpen(
        isShown: Boolean,
        isStarting: Boolean,
        openedVault: String?,
        loadedVault: String?
    ): Boolean =
        isShown &&
            !isStarting &&
            (openedVault == null || openedVault == loadedVault)
}
