package app.notekea.shell.presentation.onboarding

/** What the person does in the first-run welcome. */
sealed interface OnboardingIntent {
    data object OnContinue : OnboardingIntent

    data object OnBack : OnboardingIntent

    /** Create a vault…: the welcome screen's form is filled first, with the sheet out of its way. */
    data object OnWait : OnboardingIntent

    /** A vault was opened, by the notes step or already at startup: the AI step comes next. */
    data object OnVaultOpened : OnboardingIntent

    /** Skip, Esc, Not now or Download and start: the welcome closes and is not shown again. */
    data object OnFinish : OnboardingIntent
}
