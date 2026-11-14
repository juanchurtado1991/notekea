package app.notekea.core.notes.di

import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FuzzyMatcher
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NewNoteDefaults
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.index.ChunkNoteUseCase
import app.notekea.core.notes.domain.links.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.LinkFinder
import app.notekea.core.notes.domain.links.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.links.UpdateLinksAfterRenameUseCase
import app.notekea.core.notes.domain.templates.FillTemplateUseCase
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
    factoryOf(::NewNoteDefaults)
    factoryOf(::TextFolder)
}
