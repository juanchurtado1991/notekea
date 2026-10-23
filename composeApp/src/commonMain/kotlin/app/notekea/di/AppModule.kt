package app.notekea.di

import app.notekea.core.platform.di.platformModule
import org.koin.dsl.module

/** Composition root of the shared code: includes every feature module. */
val appModule = module {
    includes(platformModule)
}
