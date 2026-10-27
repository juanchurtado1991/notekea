package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.SavePropertiesFoldUseCase
import app.notekea.notes.domain.GetPropertiesFoldUseCase
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.FakeLocalTimeFormatter
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.AttachFileUseCase
import app.notekea.notes.domain.FakeAttachmentPicker
import app.notekea.notes.domain.FakeAttachmentStore
import app.notekea.notes.domain.FakeClipboardReader
import app.notekea.notes.domain.FakeEditorPreferencesStore
import app.notekea.notes.domain.FakeFileOpener
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeNoteFileOperations
import app.notekea.notes.domain.FakeNoteScanner
import app.notekea.notes.domain.FakeVaultSettingsReader
import app.notekea.notes.domain.FillTemplateUseCase
import app.notekea.notes.domain.GetEditorModeUseCase
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RenameAndUpdateLinksUseCase
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.SaveEditorModeUseCase
import app.notekea.notes.domain.SaveNoteUseCase
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.time.Instant

/**
 * The fakes behind an [EditorViewModel] and the ViewModel over them with real use cases, shared by the editor's
 * ViewModel tests. [attachmentStore] sees the files [operations] holds, as the disk would.
 */
class EditorViewModels(dispatcher: CoroutineDispatcher) {
    val scanner = FakeNoteScanner()
    val store = FakeNoteContentStore()
    val operations = FakeNoteFileOperations()
    val preferences = FakeEditorPreferencesStore()
    val attachmentStore = FakeAttachmentStore(fileOperations = operations)
    val settings = FakeVaultSettingsReader()
    val picker = FakeAttachmentPicker()
    val clipboard = FakeClipboardReader()
    val opener = FakeFileOpener()
    private val dispatchers = FakeAppDispatchers(dispatcher = dispatcher)

    fun create(): EditorViewModel = EditorViewModel(
        vaultPath = VAULT,
        openNote = OpenNoteUseCase(noteContentStore = store),
        saveNote = SaveNoteUseCase(noteContentStore = store),
        observeNoteOnDisk = ObserveNoteOnDiskUseCase(noteScanner = scanner, noteContentStore = store),
        renameAndUpdateLinks = RenameAndUpdateLinksUseCase(
            noteScanner = scanner,
            renameEntry = RenameEntryUseCase(
                noteFileOperations = operations,
                validateFileName = ValidateFileNameUseCase()
            ),
            updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
                planLinkUpdates = PlanLinkUpdatesUseCase(
                    noteContentStore = store,
                    linkFinder = JetBrainsMarkdownLinkFinder()
                ),
                applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)
            )
        ),
        getEditorMode = GetEditorModeUseCase(editorPreferencesStore = preferences),
        saveEditorMode = SaveEditorModeUseCase(editorPreferencesStore = preferences),
        attachmentTools = AttachmentTools(
            attachmentStore = attachmentStore,
            vaultSettingsReader = settings,
            attachFile = AttachFileUseCase(
                attachmentStore = attachmentStore,
                vaultSettingsReader = settings,
                noteFileOperations = operations,
                localTimeFormatter = FakeLocalTimeFormatter(),
                clock = FakeClock(currentTime = Instant.parse(input = NOW))
            ),
            fileOpener = opener,
            attachmentPicker = picker,
            clipboardReader = clipboard,
            dispatchers = dispatchers
        ),
        fillTemplate = fillTemplate(),
        propertyTools = propertyTools()
    )

    private fun propertyTools(): PropertyTools = PropertyTools(
        getPropertiesFold = GetPropertiesFoldUseCase(editorPreferencesStore = preferences),
        savePropertiesFold = SavePropertiesFoldUseCase(editorPreferencesStore = preferences),
        localCalendar = FakeLocalCalendar(),
        clock = FakeClock(currentTime = Instant.parse(input = NOW))
    )

    private fun fillTemplate(): FillTemplateUseCase = FillTemplateUseCase(
        vaultSettingsReader = settings,
        noteContentStore = store,
        localCalendar = FakeLocalCalendar(),
        clock = FakeClock(currentTime = Instant.parse(input = NOW))
    )

    companion object {
        private const val NOW = "2026-10-26T14:30:05Z"
    }
}
