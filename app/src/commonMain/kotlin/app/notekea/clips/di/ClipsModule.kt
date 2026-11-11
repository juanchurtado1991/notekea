package app.notekea.clips.di

import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.SupportCheck
import app.notekea.clips.presentation.ClipViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Web clips (4.4): a page the user named, clipped with notes the article backs. */
val clipsModule = module {
    factoryOf(::SupportCheck)
    factoryOf(::ClipWebPageUseCase)

    viewModel { parameters ->
        ClipViewModel(
            vaultPath = parameters.get(),
            clipWebPage = get(),
            noteContentStore = get(),
            noteFileOperations = get(),
            localCalendar = get(),
            clock = get()
        )
    }
}
