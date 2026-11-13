package app.notekea.clips.di

import app.notekea.clips.domain.ClipSectionUseCase
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.DescribeSectionUseCase
import app.notekea.clips.domain.FindSectionUseCase
import app.notekea.clips.domain.SectionRanker
import app.notekea.clips.domain.SupportCheck
import app.notekea.clips.domain.UpdateWebSourcesUseCase
import app.notekea.clips.presentation.ClipViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Web clips (4.4): a page the user named, clipped with notes the article backs. */
val clipsModule = module {
    factoryOf(::SupportCheck)
    factoryOf(::ClipWebPageUseCase)
    factoryOf(::SectionRanker)
    factoryOf(::DescribeSectionUseCase)
    factoryOf(::FindSectionUseCase)
    factoryOf(::ClipSectionUseCase)
    factoryOf(::UpdateWebSourcesUseCase)

    viewModel { parameters ->
        ClipViewModel(
            vaultPath = parameters.get(),
            clipWebPage = get(),
            findSection = get(),
            clipSection = get(),
            describeSection = get(),
            updateWebSources = get(),
            noteContentStore = get(),
            noteFileOperations = get(),
            localCalendar = get(),
            clock = get()
        )
    }
}
