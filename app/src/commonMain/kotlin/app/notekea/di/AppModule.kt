package app.notekea.di

import app.notekea.core.di.coreModule
import app.notekea.core.platform.di.platformModule
import app.notekea.core.notes.di.coreNotesModule
import app.notekea.editor.di.editorModule
import app.notekea.graph.di.graphModule
import app.notekea.ai.di.modelsModule
import app.notekea.query.di.queryModule
import app.notekea.tasks.di.tasksModule
import app.notekea.tree.di.treeModule
import app.notekea.search.di.searchModule
import app.notekea.settings.di.settingsModule
import app.notekea.connections.di.connectionsModule
import app.notekea.templates.di.templatesModule
import app.notekea.shell.di.shellModule
import app.notekea.vault.di.vaultModule
import org.koin.dsl.module

/** Composition root of the shared code: includes every feature module. */
val appModule = module {
    includes(
        coreModule,
        platformModule,
        coreNotesModule,
        editorModule,
        treeModule,
        searchModule,
        settingsModule,
        connectionsModule,
        graphModule,
        modelsModule,
        queryModule,
        tasksModule,
        templatesModule,
        shellModule,
        vaultModule
    )
}
