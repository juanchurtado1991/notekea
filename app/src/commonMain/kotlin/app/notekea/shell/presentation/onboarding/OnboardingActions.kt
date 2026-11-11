package app.notekea.shell.presentation.onboarding

/** What the welcome's buttons do, given together: the steps, the three ways to choose notes, and the download. */
data class OnboardingActions(
    val onContinue: () -> Unit = {},
    val onBack: () -> Unit = {},
    val onFinish: () -> Unit = {},
    val onOpenFolder: () -> Unit = {},
    val onCreateVault: () -> Unit = {},
    val onSampleVault: () -> Unit = {},
    val onDownload: () -> Unit = {}
)
