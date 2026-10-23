package app.notekea.core.platform.data

import app.notekea.core.platform.domain.SystemEnvironment

private const val OS_NAME_PROPERTY = "os.name"
private const val USER_HOME_PROPERTY = "user.home"

/** Reads the JVM system properties and environment variables. The only place that reads `os.name`. */
class JvmSystemEnvironment : SystemEnvironment {
    override val operatingSystemName: String = System.getProperty(OS_NAME_PROPERTY).orEmpty()
    override val userHome: String = System.getProperty(USER_HOME_PROPERTY).orEmpty()

    override fun environmentVariable(
        name: String
    ): String? = System.getenv(name)
}
