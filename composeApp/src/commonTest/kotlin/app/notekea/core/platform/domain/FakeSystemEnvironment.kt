package app.notekea.core.platform.domain

/** Scriptable [SystemEnvironment] that lets every operating system be simulated from any machine. */
class FakeSystemEnvironment(
    override val operatingSystemName: String,
    override val userHome: String,
    private val environmentVariables: Map<String, String> = emptyMap()
) : SystemEnvironment {
    override fun environmentVariable(
        name: String
    ): String? = environmentVariables[name]
}
