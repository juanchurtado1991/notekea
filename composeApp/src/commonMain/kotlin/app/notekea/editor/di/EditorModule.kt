package app.notekea.editor.di

import app.notekea.editor.data.SettingsEditorPreferencesStore
import app.notekea.editor.domain.AttachFileUseCase
import app.notekea.editor.domain.EditorPreferencesStore
import app.notekea.editor.domain.GetNoteStatsUseCase
import app.notekea.editor.domain.KeepUnsavedTextUseCase
import app.notekea.editor.domain.ObserveNoteOnDiskUseCase
import app.notekea.editor.domain.OpenNoteUseCase
import app.notekea.editor.domain.RenameAndUpdateLinksUseCase
import app.notekea.editor.domain.SaveNoteUseCase
import app.notekea.editor.presentation.attachments.AttachmentTools
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.editor.presentation.noteinfo.NoteInfoTools
import app.notekea.editor.presentation.properties.PropertyTools
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The note being edited: saving, watching its file, attachments and properties, one view model per pane.
 */
val editorModule = module {
    single<EditorPreferencesStore> { SettingsEditorPreferencesStore(settings = get(), dispatchers = get()) }
    factoryOf(::KeepUnsavedTextUseCase)
    factoryOf(::PropertyTools)
    factoryOf(::NoteInfoTools)
    factoryOf(::GetNoteStatsUseCase)
    factoryOf(::AttachFileUseCase)
    factoryOf(::AttachmentTools)
    factoryOf(::ObserveNoteOnDiskUseCase)
    factoryOf(::OpenNoteUseCase)
    factoryOf(::SaveNoteUseCase)
    factoryOf(::RenameAndUpdateLinksUseCase)
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
}
