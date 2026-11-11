package app.notekea.shell.presentation.onboarding

/** The model the AI step offers: its [name] ("Medium") and download [size] ("2.7 GB"), the one this computer fits. */
data class OnboardingAiChoice(
    val name: String,
    val size: String
)
