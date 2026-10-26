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

    /** The packaged app's version, or "development" when it runs from the source tree. */
    val appVersion: String

    fun environmentVariable(
        name: String
    ): String?
}
