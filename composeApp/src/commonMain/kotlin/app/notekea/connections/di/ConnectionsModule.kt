package app.notekea.connections.di

import app.notekea.connections.data.JetBrainsMarkdownMentionFinder
import app.notekea.connections.domain.FindMentionsUseCase
import app.notekea.connections.domain.FindRelatedNotesUseCase
import app.notekea.connections.domain.LinkMentionUseCase
import app.notekea.connections.domain.MentionFinder
import app.notekea.connections.presentation.NoteMentionsViewModel
import app.notekea.connections.presentation.RelatedNotesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Related notes and mentions, shown in the inspector.
 */
val connectionsModule = module {
    single<MentionFinder> { JetBrainsMarkdownMentionFinder() }
    factoryOf(::FindRelatedNotesUseCase)
    factoryOf(::FindMentionsUseCase)
    factoryOf(::LinkMentionUseCase)
    viewModel { parameters -> RelatedNotesViewModel(vaultPath = parameters.get(), findRelatedNotes = get()) }
    viewModel { parameters ->
        NoteMentionsViewModel(
            vaultPath = parameters.get(),
            findMentions = get(),
            linkMention = get(),
            noteTextCache = get(),
            dispatchers = get()
        )
    }
}
