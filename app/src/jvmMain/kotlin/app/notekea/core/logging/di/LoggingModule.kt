package app.notekea.core.logging.di

import app.notekea.core.logging.data.AwtTextClipboard
import app.notekea.core.logging.data.FileLogWriter
import app.notekea.core.logging.data.LogFiles
import app.notekea.core.logging.data.OkioLogStore
import app.notekea.core.logging.domain.LogStore
import app.notekea.core.logging.domain.TextClipboard
import app.notekea.core.platform.domain.PlatformConventions
import okio.FileSystem
import org.koin.dsl.module
import java.nio.file.Path

/** The log file in the app data folder, what Copy diagnostics reads from it, and the clipboard it copies to. */
val loggingModule = module {
    single {
        FileLogWriter(
            directory = Path.of(get<PlatformConventions>().appDataDirectory, LogFiles.FOLDER),
            clock = get()
        )
    }

    single<LogStore> {
        OkioLogStore(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = get(),
            appDataDirectory = get<PlatformConventions>().appDataDirectory
        )
    }

    single<TextClipboard> { AwtTextClipboard() }
}
