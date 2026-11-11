package app.notekea.shell.presentation.onboarding

/**
 * The first-run welcome: whether it is shown ([isShown]), at which [step], and whether it waits behind the welcome
 * screen while a new vault is named there ([isWaiting]).
 */
data class OnboardingState(
    val isShown: Boolean = false,
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val isWaiting: Boolean = false
)
