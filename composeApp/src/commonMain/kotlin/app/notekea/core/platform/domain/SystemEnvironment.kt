package app.notekea.core.platform.domain

/**
 * Raw facts about the machine, read by the platform layer and nothing else. All decisions based on them live in
 * [ResolvePlatformConventionsUseCase], so they can be tested for every operating system from any machine.
 */
interface SystemEnvironment {
    val operatingSystemName: String
    val userHome: String

    fun environmentVariable(
        name: String
    ): String?
}
