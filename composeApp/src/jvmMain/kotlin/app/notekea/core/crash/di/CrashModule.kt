package app.notekea.core.crash.di

import app.notekea.core.crash.data.OkioCrashReportStore
import app.notekea.core.crash.domain.CrashReportStore
import app.notekea.core.crash.domain.RecordCrashUseCase
import app.notekea.core.crash.presentation.CrashNotice
import app.notekea.core.crash.presentation.CrashReportActions
import app.notekea.core.crash.presentation.CrashReporter
import app.notekea.core.crash.presentation.SwingCrashNotice
import app.notekea.core.platform.domain.PlatformConventions
import okio.FileSystem
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Crash reports in the app data folder, what the notice's buttons do, and the Swing notice used as a fallback. */
val crashModule = module {
    single<CrashReportStore> {
        OkioCrashReportStore(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = get(),
            appDataDirectory = get<PlatformConventions>().appDataDirectory
        )
    }
    single { CrashReportActions() }
    single<CrashNotice> { SwingCrashNotice(actions = get()) }
    factoryOf(::RecordCrashUseCase)
    single { CrashReporter(recordCrash = get(), fallbackNotice = get()) }
}
