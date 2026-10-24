package app.notekea.core.di

import app.notekea.core.domain.time.DescribeElapsedTimeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import kotlin.time.Clock

/** Cross-feature services: the system clock, read only here (`ARCHITECTURE.md` §5), and time descriptions. */
val coreModule = module {
    single<Clock> { Clock.System }
    factoryOf(::DescribeElapsedTimeUseCase)
}
