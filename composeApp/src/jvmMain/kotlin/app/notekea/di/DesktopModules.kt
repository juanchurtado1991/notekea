package app.notekea.di

import app.notekea.core.embedding.di.embeddingModule
import app.notekea.core.logging.di.loggingModule
import app.notekea.core.platform.di.jvmPlatformModule
import app.notekea.core.notes.di.jvmCoreNotesModule
import app.notekea.tree.di.jvmTreeModule
import app.notekea.editor.di.jvmEditorModule
import app.notekea.vault.di.jvmVaultModule
import org.koin.core.module.Module

/** Every Koin module of the desktop app: the shared composition root plus the JVM bindings. */
val desktopModules: List<Module> = listOf(
    appModule,
    loggingModule,
    embeddingModule,
    jvmPlatformModule,
    jvmCoreNotesModule,
    jvmTreeModule,
    jvmEditorModule,
    jvmVaultModule
)
