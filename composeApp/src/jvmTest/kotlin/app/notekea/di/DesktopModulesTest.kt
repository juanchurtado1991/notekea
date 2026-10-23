package app.notekea.di

import app.notekea.core.platform.domain.PlatformConventions
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Builds the real desktop dependency graph (no global Koin) and resolves its root bindings. */
class DesktopModulesTest {
    private val koinApplication = koinApplication { modules(desktopModules) }

    @AfterTest
    fun tearDown() {
        koinApplication.close()
    }

    @Test
    fun `platform conventions resolve once from the real environment`() {
        val koin = koinApplication.koin
        val first = koin.get<PlatformConventions>()
        val second = koin.get<PlatformConventions>()

        assertSame(
            expected = first,
            actual = second
        )
        assertTrue(actual = first.appDataDirectory.isNotBlank())
    }
}
