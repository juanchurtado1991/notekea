package app.notekea.core.platform.di

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Resolves the [PlatformConventions] once per app run; the `SystemEnvironment` comes from the platform module. */
val platformModule = module {
    factoryOf(::ResolvePlatformConventionsUseCase)
    single<PlatformConventions> { get<ResolvePlatformConventionsUseCase>().invoke() }
}
