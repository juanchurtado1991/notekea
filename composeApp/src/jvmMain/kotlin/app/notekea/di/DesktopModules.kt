package app.notekea.di

import app.notekea.core.crash.di.crashModule
import app.notekea.core.logging.di.loggingModule
import app.notekea.core.platform.di.jvmPlatformModule
import app.notekea.notes.di.jvmNotesModule
import app.notekea.vault.di.jvmVaultModule
import org.koin.core.module.Module

/** Every Koin module of the desktop app: the shared composition root plus the JVM bindings. */
val desktopModules: List<Module> = listOf(
    appModule,
    crashModule,
    loggingModule,
    jvmPlatformModule,
    jvmNotesModule,
    jvmVaultModule
)
