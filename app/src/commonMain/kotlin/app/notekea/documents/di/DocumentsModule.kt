package app.notekea.documents.di

import app.notekea.documents.presentation.DocumentViewerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Documents (4.5): each one's read-only tab. Reading them is core's ([app.notekea.core.documents.domain]). */
val documentsModule = module {
    viewModel { parameters ->
        DocumentViewerViewModel(
            vaultPath = parameters.get(),
            path = parameters.get(),
            reader = get(),
            pdfPages = get(),
            fileOpener = get(),
            dispatchers = get()
        )
    }
}
