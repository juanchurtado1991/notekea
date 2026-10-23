package app.notekea.core.platform.di

import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.domain.SystemEnvironment
import org.koin.dsl.module

/** Binds the desktop implementations of the platform ports. */
val jvmPlatformModule = module {
    single<SystemEnvironment> { JvmSystemEnvironment() }
}
