package app.notekea.core.platform.di

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.PropertiesFileSettingsFactory
import app.notekea.core.data.SqliteDatabaseFactory
import app.notekea.db.NotekeaDatabase
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemEnvironment
import com.russhwolf.settings.Settings
import org.koin.dsl.module

/** Binds the desktop implementations of the platform ports, the dispatchers, the app settings file and database. */
val jvmPlatformModule = module {
    single<AppDispatchers> { JvmAppDispatchers() }
    single<Settings> {
        PropertiesFileSettingsFactory(appDataDirectory = get<PlatformConventions>().appDataDirectory).create()
    }
    single<NotekeaDatabase> {
        SqliteDatabaseFactory(appDataDirectory = get<PlatformConventions>().appDataDirectory).create()
    }
    single<SystemEnvironment> { JvmSystemEnvironment() }
}
