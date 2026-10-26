package app.notekea.core.platform.domain

/** Scriptable [SystemEnvironment] that lets every operating system be simulated from any machine. */
class FakeSystemEnvironment(
    override val operatingSystemName: String,
    override val userHome: String,
    private val environmentVariables: Map<String, String> = emptyMap(),
    override val operatingSystemVersion: String = "7.2",
    override val javaVersion: String = "21.0.11",
    override val appVersion: String = "1.0.0"
) : SystemEnvironment {
    override fun environmentVariable(
        name: String
    ): String? = environmentVariables[name]
}
