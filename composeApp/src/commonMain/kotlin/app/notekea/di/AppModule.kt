package app.notekea.di

import app.notekea.core.di.coreModule
import app.notekea.core.platform.di.platformModule
import app.notekea.notes.di.notesModule
import app.notekea.vault.di.vaultModule
import org.koin.dsl.module

/** Composition root of the shared code: includes every feature module. */
val appModule = module {
    includes(coreModule, platformModule, notesModule, vaultModule)
}
