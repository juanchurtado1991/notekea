package app.notekea.core.platform.di

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.JvmLocalTimeFormatter
import app.notekea.core.data.PropertiesFileSettingsFactory
import app.notekea.core.data.SqliteDatabaseFactory
import app.notekea.db.NotekeaDatabase
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.LocalTimeFormatter
import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.data.PortalSystemAppearance
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemAppearance
import app.notekea.core.platform.domain.SystemEnvironment
import com.russhwolf.settings.Settings
import org.koin.dsl.module

/** Binds the desktop implementations of the platform ports (the system's dark theme too), the dispatchers, the app settings file and database. */
val jvmPlatformModule = module {
    single<AppDispatchers> { JvmAppDispatchers() }
    single<LocalTimeFormatter> { JvmLocalTimeFormatter() }
    single<Settings> {
        PropertiesFileSettingsFactory(appDataDirectory = get<PlatformConventions>().appDataDirectory).create()
    }
    single<NotekeaDatabase> {
        SqliteDatabaseFactory(appDataDirectory = get<PlatformConventions>().appDataDirectory).create()
    }
    single<SystemEnvironment> { JvmSystemEnvironment() }
    single<SystemAppearance> {
        PortalSystemAppearance(operatingSystem = get<PlatformConventions>().operatingSystem, dispatchers = get())
    }
}
