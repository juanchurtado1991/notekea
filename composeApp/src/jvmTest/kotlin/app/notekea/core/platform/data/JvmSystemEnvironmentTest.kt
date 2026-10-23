package app.notekea.core.platform.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JvmSystemEnvironmentTest {
    private val environment = JvmSystemEnvironment()

    @Test
    fun `missing environment variables are null`() {
        assertNull(actual = environment.environmentVariable(name = MISSING_VARIABLE))
    }

    @Test
    fun `operating system name and user home come from the JVM properties`() {
        assertEquals(
            expected = System.getProperty(OS_NAME_PROPERTY),
            actual = environment.operatingSystemName
        )
        assertEquals(
            expected = System.getProperty(USER_HOME_PROPERTY),
            actual = environment.userHome
        )
        assertTrue(actual = environment.userHome.isNotBlank())
    }

    companion object {
        private const val MISSING_VARIABLE = "NOTEKEA_TEST_VARIABLE_THAT_DOES_NOT_EXIST"
        private const val OS_NAME_PROPERTY = "os.name"
        private const val USER_HOME_PROPERTY = "user.home"
    }
}
