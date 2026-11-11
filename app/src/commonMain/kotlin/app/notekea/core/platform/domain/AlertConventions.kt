package app.notekea.core.platform.domain

/**
 * Whether a confirmation stacks its buttons, the safe one under the action, as macOS's alerts do (macOS redesign 8A).
 * Windows and Linux keep a row in [PlatformConventions.dialogButtonOrder].
 */
val PlatformConventions.stacksAlertButtons: Boolean
    get() = operatingSystem == OperatingSystem.MACOS
