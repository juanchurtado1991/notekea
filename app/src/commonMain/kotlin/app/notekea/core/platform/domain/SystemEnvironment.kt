package app.notekea.core.platform.domain

/**
 * Raw facts about the machine, read by the platform layer and nothing else. All decisions based on them live in
 * [ResolvePlatformConventionsUseCase], so they can be tested for every operating system from any machine.
 */
interface SystemEnvironment {
    val operatingSystemName: String
    val operatingSystemVersion: String
    val userHome: String

    /** The Java runtime the app runs on. */
    val javaVersion: String

    /** The version people see (About, crash reports), or "development" when the build did not set it, as in tests. */
    val appVersion: String

    fun environmentVariable(
        name: String
    ): String?
}
