package app.notekea.notes.di

import app.notekea.notes.presentation.editor.NoteInfoTools
import app.notekea.notes.presentation.editor.PropertyTools
import app.notekea.notes.data.SettingsBookmarkStore
import app.notekea.notes.domain.BookmarkStore
import app.notekea.core.notes.data.GhostFrontmatterParser
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.connections.data.JetBrainsMarkdownMentionFinder
import app.notekea.connections.domain.FindMentionsUseCase
import app.notekea.notes.domain.KeepUnsavedTextUseCase
import app.notekea.connections.domain.LinkMentionUseCase
import app.notekea.connections.domain.MentionFinder
import app.notekea.connections.presentation.NoteMentionsViewModel
import app.notekea.notes.data.SettingsEditorPreferencesStore
import app.notekea.notes.data.SettingsTreePreferencesStore
import app.notekea.notes.data.SqlDelightNoteMetadataCache
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.AttachFileUseCase
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.EditorPreferencesStore
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.data.SettingsWorkspaceStore
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.WorkspaceStore
import app.notekea.shell.presentation.WorkspaceTools
import app.notekea.shell.presentation.WorkspaceViewModel
import app.notekea.notes.domain.GetDailyNoteSettingsUseCase
import app.notekea.notes.domain.OpenDailyNoteUseCase
import app.notekea.templates.presentation.TemplatePickerViewModel
import app.notekea.templates.domain.CreateTemplatesFolderUseCase
import app.notekea.notes.domain.ApplyTemplateUseCase
import app.notekea.core.notes.domain.FillTemplateUseCase
import app.notekea.templates.domain.GetTemplateSettingsUseCase
import app.notekea.notes.presentation.tree.DailyNoteTools
import app.notekea.notes.presentation.tree.NewNoteTools
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.FollowLinkUseCase
import app.notekea.core.notes.domain.FuzzyMatcher
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.notes.domain.GetNoteStatsUseCase
import app.notekea.core.notes.domain.LinkFinder
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteMetadataCache
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.core.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RenameAndUpdateLinksUseCase
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.SaveNoteUseCase
import app.notekea.search.domain.SearchNoteContentUseCase
import app.notekea.search.domain.SearchNotesUseCase
import app.notekea.core.notes.domain.TextFolder
import app.notekea.notes.domain.TreePreferencesStore
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.notes.presentation.editor.AttachmentTools
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.search.presentation.switcher.QuickSwitcherViewModel
import app.notekea.notes.presentation.tags.TagPickerViewModel
import app.notekea.notes.presentation.tree.VaultTreeViewModel
import app.notekea.search.data.SqlDelightIndexStateStore
import app.notekea.core.notes.domain.ChunkNoteUseCase
import app.notekea.search.domain.IndexStateStore
import app.notekea.search.domain.IndexVaultUseCase
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.search.domain.PlanIndexUseCase
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.connections.domain.FindRelatedNotesUseCase
import app.notekea.connections.presentation.RelatedNotesViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Notes feature: the vault tree use cases, its preferences on the app settings, its note cache in the app database
 * and the sidebar ViewModel, created
 * once per vault with the vault path as its parameter. Platform adapters are bound by the platform module.
 */
val notesModule = module {
    single<LinkFinder> { JetBrainsMarkdownLinkFinder() }
    single<MentionFinder> { JetBrainsMarkdownMentionFinder() }
    single<NoteMetadataCache> { SqlDelightNoteMetadataCache(database = get(), dispatchers = get()) }
    single<IndexStateStore> { SqlDelightIndexStateStore(database = get(), dispatchers = get()) }
    single<TreePreferencesStore> { SettingsTreePreferencesStore(settings = get(), dispatchers = get()) }
    single<EditorPreferencesStore> { SettingsEditorPreferencesStore(settings = get(), dispatchers = get()) }
    single<BookmarkStore> { SettingsBookmarkStore(settings = get(), dispatchers = get()) }
    single<WorkspaceStore> { SettingsWorkspaceStore(settings = get(), dispatchers = get()) }
    factoryOf(::OutlineFinder)
    factoryOf(::NoteSections)
    factoryOf(::ChunkNoteUseCase)
    factoryOf(::PlanIndexUseCase)
    factoryOf(::IndexVaultUseCase)
    factoryOf(::FindRelatedNotesUseCase)
    factoryOf(::KeepUnsavedTextUseCase)
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
    viewModel { parameters ->
        SearchIndexViewModel(
            vaultPath = parameters.get(),
            indexVault = get(),
            vectorIndex = get(),
            indexStateStore = get(),
            describeElapsedTime = get(),
            clock = get()
        )
    }
    factoryOf(::GetWorkspaceUseCase)
    factoryOf(::GetNoteCoverUseCase)
    factoryOf(::WorkspaceTools)
    factoryOf(::GetSidePanelUseCase)
    factoryOf(::GetDailyNoteSettingsUseCase)
    factoryOf(::OpenDailyNoteUseCase)
    factoryOf(::GetTemplateSettingsUseCase)
    factoryOf(::FillTemplateUseCase)
    factoryOf(::ApplyTemplateUseCase)
    factoryOf(::CreateTemplatesFolderUseCase)
    factoryOf(::DailyNoteTools)
    factoryOf(::NewNoteTools)
    factoryOf(::PropertyTools)
    factoryOf(::NoteInfoTools)
    factoryOf(::GetNoteStatsUseCase)
    factoryOf(::SaveSidePanelUseCase)
    factoryOf(::ApplyLinkUpdatesUseCase)
    factoryOf(::AttachFileUseCase)
    factoryOf(::AttachmentTools)
    factoryOf(::CreateFolderUseCase)
    factoryOf(::CreateNoteUseCase)
    factoryOf(::FlattenTreeUseCase)
    factoryOf(::FuzzyMatcher)
    factoryOf(::MarkdownLineStripper)
    factoryOf(::FollowLinkUseCase)
    factoryOf(::GhostFrontmatterParser)
    factoryOf(::NaturalNameComparator)
    factoryOf(::NoteTextCache)
    factoryOf(::ObserveVaultTreeUseCase)
    factoryOf(::ObserveNoteOnDiskUseCase)
    factoryOf(::OpenNoteUseCase)
    factoryOf(::PlanLinkUpdatesUseCase)
    factoryOf(::SaveNoteUseCase)
    factoryOf(::UpdateLinksAfterRenameUseCase)
    factoryOf(::RenameAndUpdateLinksUseCase)
    factoryOf(::RenameEntryUseCase)
    factoryOf(::SearchNoteContentUseCase)
    factoryOf(::SearchNotesUseCase)
    factoryOf(::TextFolder)
    viewModel { parameters ->
        VaultTreeViewModel(
            vaultPath = parameters.get(),
            platformConventions = get(),
            observeVaultTree = get(),
            flattenTree = get(),
            newNotes = get(),
            createFolder = get(),
            renameEntry = get(),
            trash = get(),
            fileRevealer = get(),
            fileOpener = get(),
            days = get(),
            bookmarkStore = get(),
            updateLinksAfterRename = get(),
            followLink = get(),
            treePreferencesStore = get()
        )
    }
    viewModel { parameters ->
        EditorViewModel(
            vaultPath = parameters.get(),
            openNote = get(),
            saveNote = get(),
            observeNoteOnDisk = get(),
            renameAndUpdateLinks = get(),
            editorPreferences = get(),
            attachmentTools = get(),
            fillTemplate = get(),
            propertyTools = get(),
            noteInfoTools = get(),
            keepUnsavedText = get()
        )
    }
    viewModel { TagPickerViewModel() }
    viewModel { parameters ->
        TemplatePickerViewModel(
            vaultPath = parameters.get(),
            getTemplateSettings = get(),
            createTemplatesFolder = get(),
            fuzzyMatcher = get()
        )
    }
    viewModel { parameters -> WorkspaceViewModel(vaultPath = parameters.get(), tools = get()) }
    viewModel { parameters ->
        QuickSwitcherViewModel(
            vaultPath = parameters.get(),
            searchNotes = get(),
            searchNoteContent = get(),
            dispatchers = get()
        )
    }
}
