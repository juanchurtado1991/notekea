package app.notekea.core.platform.data

import app.notekea.core.platform.domain.SystemEnvironment

/**
 * Reads the JVM system properties and environment variables. The only place that reads `os.name`. The packaged app
 * knows its version from `jpackage.app-version`, which jpackage sets; run from the source tree it is "development".
 */
class JvmSystemEnvironment : SystemEnvironment {
    override val operatingSystemName: String = System.getProperty(OS_NAME_PROPERTY).orEmpty()
    override val operatingSystemVersion: String = System.getProperty(OS_VERSION_PROPERTY).orEmpty()
    override val userHome: String = System.getProperty(USER_HOME_PROPERTY).orEmpty()
    override val javaVersion: String = System.getProperty(JAVA_VERSION_PROPERTY).orEmpty()
    override val appVersion: String = System.getProperty(APP_VERSION_PROPERTY) ?: DEVELOPMENT_VERSION

    override fun environmentVariable(
        name: String
    ): String? = System.getenv(name)

    companion object {
        private const val APP_VERSION_PROPERTY = "jpackage.app-version"
        private const val DEVELOPMENT_VERSION = "development"
        private const val JAVA_VERSION_PROPERTY = "java.version"
        private const val OS_NAME_PROPERTY = "os.name"
        private const val OS_VERSION_PROPERTY = "os.version"
        private const val USER_HOME_PROPERTY = "user.home"
    }
}
