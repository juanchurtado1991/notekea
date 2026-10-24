package app.notekea.di

import app.notekea.core.platform.di.jvmPlatformModule
import app.notekea.vault.di.jvmVaultModule
import org.koin.core.module.Module

/** Every Koin module of the desktop app: the shared composition root plus the JVM bindings. */
val desktopModules: List<Module> = listOf(
    appModule,
    jvmPlatformModule,
    jvmVaultModule
)
