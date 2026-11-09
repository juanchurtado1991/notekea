package app.notekea.core.notes.di

import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.core.notes.domain.FillTemplateUseCase
import app.notekea.core.notes.domain.FuzzyMatcher
import app.notekea.core.notes.domain.LinkFinder
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * What features share about notes: the Markdown readers, the note text cache and the link updates.
 */
val coreNotesModule = module {
    single<LinkFinder> { JetBrainsMarkdownLinkFinder() }
    factoryOf(::OutlineFinder)
    factoryOf(::NoteSections)
    factoryOf(::ChunkNoteUseCase)
    factoryOf(::FillTemplateUseCase)
    factoryOf(::ApplyLinkUpdatesUseCase)
    factoryOf(::FuzzyMatcher)
    factoryOf(::MarkdownLineStripper)
    factoryOf(::GhostFrontmatterParser)
    factoryOf(::NaturalNameComparator)
    factoryOf(::NoteTextCache)
    factoryOf(::PlanLinkUpdatesUseCase)
    factoryOf(::UpdateLinksAfterRenameUseCase)
    factoryOf(::RenameEntryUseCase)
    factoryOf(::TextFolder)
}
