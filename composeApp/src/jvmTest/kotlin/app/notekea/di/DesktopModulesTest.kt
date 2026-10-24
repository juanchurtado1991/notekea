package app.notekea.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.db.NotekeaDatabase
import app.notekea.notes.domain.Trash
import app.notekea.notes.presentation.tree.VaultTreeViewModel
import app.notekea.vault.domain.RecentVaultsStore
import app.notekea.vault.presentation.welcome.WelcomeViewModel
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Builds the real desktop dependency graph (no global Koin) and resolves its root bindings. The settings file is
 * and database are replaced by in-memory ones and the main dispatcher is never advanced, so nothing touches the user's
 * data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DesktopModulesTest {
    private val inMemorySettings = module {
        single<Settings> { MapSettings() }
        single<NotekeaDatabase> {
            val driver = JdbcSqliteDriver(url = JdbcSqliteDriver.IN_MEMORY)
            NotekeaDatabase.Schema.create(driver)
            NotekeaDatabase(driver = driver)
        }
    }
    private val koinApplication = koinApplication {
        allowOverride(override = true)
        modules(desktopModules + inMemorySettings)
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        koinApplication.close()
        Dispatchers.resetMain()
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

    @Test
    fun `the vault tree resolves for a vault path with all its adapters`() {
        val koin = koinApplication.koin

        assertNotNull(actual = koin.get<VaultTreeViewModel> { parametersOf(VAULT_PATH) })
        assertSame(
            expected = koin.get<Trash>(),
            actual = koin.get<Trash>()
        )
    }

    @Test
    fun `the welcome flow resolves with all its use cases`() {
        val koin = koinApplication.koin

        assertNotNull(actual = koin.get<WelcomeViewModel>())
        assertSame(
            expected = koin.get<RecentVaultsStore>(),
            actual = koin.get<RecentVaultsStore>()
        )
    }

    companion object {
        // Never read: the main dispatcher is not advanced, so the ViewModel never starts scanning.
        private const val VAULT_PATH = "notekea-unused-vault"
    }
}
