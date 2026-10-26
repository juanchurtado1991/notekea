package app.notekea.core.crash.di

import app.notekea.core.crash.data.OkioCrashReportStore
import app.notekea.core.crash.domain.CrashReportStore
import app.notekea.core.crash.domain.RecordCrashUseCase
import app.notekea.core.crash.presentation.CrashNotice
import app.notekea.core.crash.presentation.CrashReporter
import app.notekea.core.crash.presentation.SwingCrashNotice
import app.notekea.core.platform.domain.PlatformConventions
import okio.FileSystem
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Crash reports in the app data folder and the Swing notice shown when the window fails. */
val crashModule = module {
    single<CrashReportStore> {
        OkioCrashReportStore(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = get(),
            appDataDirectory = get<PlatformConventions>().appDataDirectory
        )
    }
    single<CrashNotice> { SwingCrashNotice() }
    factoryOf(::RecordCrashUseCase)
    single { CrashReporter(recordCrash = get(), crashNotice = get()) }
}
