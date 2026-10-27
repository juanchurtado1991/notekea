package app.notekea.notes.di

import app.notekea.notes.domain.SavePropertiesFoldUseCase
import app.notekea.notes.domain.GetPropertiesFoldUseCase
import app.notekea.notes.presentation.editor.PropertyTools
import app.notekea.notes.presentation.tree.BookmarkTools
import app.notekea.notes.domain.SaveBookmarksUseCase
import app.notekea.notes.domain.GetBookmarksUseCase
import app.notekea.notes.data.SettingsBookmarkStore
import app.notekea.notes.domain.BookmarkStore
import app.notekea.notes.data.GhostFrontmatterParser
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.data.SettingsEditorPreferencesStore
import app.notekea.notes.data.SettingsTreePreferencesStore
import app.notekea.notes.data.SqlDelightNoteMetadataCache
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.AttachFileUseCase
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.DeleteEntryUseCase
import app.notekea.notes.domain.EditorPreferencesStore
import app.notekea.notes.domain.GetSidePanelUseCase
import app.notekea.notes.data.SettingsWorkspaceStore
import app.notekea.notes.domain.GetNoteCoverUseCase
import app.notekea.notes.domain.GetWorkspaceUseCase
import app.notekea.notes.domain.SaveWorkspaceUseCase
import app.notekea.notes.domain.WorkspaceStore
import app.notekea.notes.presentation.workspace.WorkspaceTools
import app.notekea.notes.presentation.workspace.WorkspaceViewModel
import app.notekea.notes.domain.GetDailyNoteSettingsUseCase
import app.notekea.notes.domain.OpenDailyNoteUseCase
import app.notekea.notes.presentation.templates.TemplatePickerViewModel
import app.notekea.notes.domain.CreateTemplatesFolderUseCase
import app.notekea.notes.domain.ApplyTemplateUseCase
import app.notekea.notes.domain.FillTemplateUseCase
import app.notekea.notes.domain.GetTemplateSettingsUseCase
import app.notekea.notes.presentation.tree.DailyNoteTools
import app.notekea.notes.presentation.tree.NewNoteTools
import app.notekea.notes.domain.SaveSidePanelUseCase
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.FollowLinkUseCase
import app.notekea.notes.domain.FuzzyMatcher
import app.notekea.notes.domain.MarkdownLineStripper
import app.notekea.notes.domain.GetEditorModeUseCase
import app.notekea.notes.domain.GetTreePreferencesUseCase
import app.notekea.notes.domain.LinkFinder
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteMetadataCache
import app.notekea.notes.domain.NoteTextCache
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RenameAndUpdateLinksUseCase
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.RevealEntryUseCase
import app.notekea.notes.domain.SaveEditorModeUseCase
import app.notekea.notes.domain.SaveNoteUseCase
import app.notekea.notes.domain.SaveTreePreferencesUseCase
import app.notekea.notes.domain.SearchNoteContentUseCase
import app.notekea.notes.domain.SearchNotesUseCase
import app.notekea.notes.domain.TextFolder
import app.notekea.notes.domain.TreePreferencesStore
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.notes.presentation.editor.AttachmentTools
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.notes.presentation.switcher.QuickSwitcherViewModel
import app.notekea.notes.presentation.tags.TagPickerViewModel
import app.notekea.notes.presentation.tree.VaultTreeViewModel
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
    single<NoteMetadataCache> { SqlDelightNoteMetadataCache(database = get(), dispatchers = get()) }
    single<TreePreferencesStore> { SettingsTreePreferencesStore(settings = get(), dispatchers = get()) }
    single<EditorPreferencesStore> { SettingsEditorPreferencesStore(settings = get(), dispatchers = get()) }
    single<BookmarkStore> { SettingsBookmarkStore(settings = get(), dispatchers = get()) }
    single<WorkspaceStore> { SettingsWorkspaceStore(settings = get(), dispatchers = get()) }
    factoryOf(::GetWorkspaceUseCase)
    factoryOf(::SaveWorkspaceUseCase)
    factoryOf(::GetNoteCoverUseCase)
    factoryOf(::WorkspaceTools)
    factoryOf(::GetEditorModeUseCase)
    factoryOf(::SaveEditorModeUseCase)
    factoryOf(::GetSidePanelUseCase)
    factoryOf(::GetDailyNoteSettingsUseCase)
    factoryOf(::OpenDailyNoteUseCase)
    factoryOf(::GetTemplateSettingsUseCase)
    factoryOf(::FillTemplateUseCase)
    factoryOf(::ApplyTemplateUseCase)
    factoryOf(::CreateTemplatesFolderUseCase)
    factoryOf(::DailyNoteTools)
    factoryOf(::NewNoteTools)
    factoryOf(::BookmarkTools)
    factoryOf(::PropertyTools)
    factoryOf(::GetPropertiesFoldUseCase)
    factoryOf(::SavePropertiesFoldUseCase)
    factoryOf(::GetBookmarksUseCase)
    factoryOf(::SaveBookmarksUseCase)
    factoryOf(::SaveSidePanelUseCase)
    factoryOf(::ApplyLinkUpdatesUseCase)
    factoryOf(::AttachFileUseCase)
    factoryOf(::AttachmentTools)
    factoryOf(::CreateFolderUseCase)
    factoryOf(::CreateNoteUseCase)
    factoryOf(::DeleteEntryUseCase)
    factoryOf(::FlattenTreeUseCase)
    factoryOf(::FuzzyMatcher)
    factoryOf(::MarkdownLineStripper)
    factoryOf(::FollowLinkUseCase)
    factoryOf(::GetTreePreferencesUseCase)
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
    factoryOf(::RevealEntryUseCase)
    factoryOf(::SaveTreePreferencesUseCase)
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
            deleteEntry = get(),
            revealEntry = get(),
            fileOpener = get(),
            days = get(),
            bookmarkTools = get(),
            updateLinksAfterRename = get(),
            followLink = get(),
            getTreePreferences = get(),
            saveTreePreferences = get()
        )
    }
    viewModel { parameters ->
        EditorViewModel(
            vaultPath = parameters.get(),
            openNote = get(),
            saveNote = get(),
            observeNoteOnDisk = get(),
            renameAndUpdateLinks = get(),
            getEditorMode = get(),
            saveEditorMode = get(),
            attachmentTools = get(),
            fillTemplate = get(),
            propertyTools = get()
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
