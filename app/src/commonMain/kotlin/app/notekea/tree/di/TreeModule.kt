package app.notekea.tree.di

import app.notekea.tree.data.SettingsBookmarkStore
import app.notekea.tree.data.SettingsTreePreferencesStore
import app.notekea.tree.data.SqlDelightNoteMetadataCache
import app.notekea.tree.domain.ApplyTemplateUseCase
import app.notekea.tree.domain.BookmarkStore
import app.notekea.tree.domain.CreateFolderUseCase
import app.notekea.tree.domain.CreateNoteUseCase
import app.notekea.tree.domain.DuplicateNoteUseCase
import app.notekea.tree.domain.FlattenTreeUseCase
import app.notekea.tree.domain.FollowLinkUseCase
import app.notekea.tree.domain.GetDailyNoteSettingsUseCase
import app.notekea.tree.domain.MoveEntryUseCase
import app.notekea.tree.domain.NoteMetadataCache
import app.notekea.tree.domain.ObserveVaultTreeUseCase
import app.notekea.tree.domain.OpenDailyNoteUseCase
import app.notekea.tree.domain.TreePreferencesStore
import app.notekea.tree.presentation.tags.TagPickerViewModel
import app.notekea.tree.presentation.viewmodel.DailyNoteTools
import app.notekea.tree.presentation.viewmodel.NewNoteTools
import app.notekea.tree.presentation.viewmodel.VaultTreeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The sidebar: the vault tree, new notes, daily notes, bookmarks and the tag picker.
 */
val treeModule = module {
    single<NoteMetadataCache> {
        SqlDelightNoteMetadataCache(
            database = get(),
            dispatchers = get()
        )
    }

    single<TreePreferencesStore> {
        SettingsTreePreferencesStore(
            settings = get(),
            dispatchers = get()
        )
    }

    single<BookmarkStore> {
        SettingsBookmarkStore(
            settings = get(),
            dispatchers = get()
        )
    }

    factoryOf(::GetDailyNoteSettingsUseCase)
    factoryOf(::OpenDailyNoteUseCase)
    factoryOf(::ApplyTemplateUseCase)
    factoryOf(::DailyNoteTools)
    factoryOf(::NewNoteTools)
    factoryOf(::CreateFolderUseCase)
    factoryOf(::DuplicateNoteUseCase)
    factoryOf(::MoveEntryUseCase)

    factory {
        CreateNoteUseCase(
            noteFileOperations = get(),
            noteContentStore = get(),
            defaults = get()
        )
    }

    factoryOf(::FlattenTreeUseCase)
    factoryOf(::FollowLinkUseCase)
    factoryOf(::ObserveVaultTreeUseCase)

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

    viewModel { TagPickerViewModel() }
}
