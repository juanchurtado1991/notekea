package app.notekea.editor.presentation.viewmodel

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.FakeLocalTimeFormatter
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeFileOpener
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.notes.domain.FakeNoteScanner
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.core.notes.domain.FillTemplateUseCase
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.editor.domain.AttachFileUseCase
import app.notekea.editor.domain.FakeAttachmentPicker
import app.notekea.editor.domain.FakeClipboardReader
import app.notekea.editor.domain.FakeEditorPreferencesStore
import app.notekea.editor.domain.FakeNoteTimesReader
import app.notekea.editor.domain.FakeYouTubeDetails
import app.notekea.editor.domain.GetNoteStatsUseCase
import app.notekea.editor.domain.KeepUnsavedTextUseCase
import app.notekea.editor.domain.ObserveNoteOnDiskUseCase
import app.notekea.editor.domain.OpenNoteUseCase
import app.notekea.editor.domain.RenameAndUpdateLinksUseCase
import app.notekea.editor.domain.SaveNoteUseCase
import app.notekea.editor.presentation.attachments.AttachmentTools
import app.notekea.editor.presentation.noteinfo.NoteInfoTools
import app.notekea.editor.presentation.properties.PropertyTools
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.time.Instant

/**
 * The fakes behind an [EditorViewModel] and the ViewModel over them with real use cases, shared by the editor's
 * ViewModel tests. [attachmentStore] sees the files [operations] holds, as the disk would.
 */
class EditorViewModels(
    dispatcher: CoroutineDispatcher,
    io: CoroutineDispatcher = dispatcher
) {
    val scanner = FakeNoteScanner()
    val store = FakeNoteContentStore()
    val operations = FakeNoteFileOperations()
    val preferences = FakeEditorPreferencesStore()
    val attachmentStore = FakeAttachmentStore(fileOperations = operations)
    val settings = FakeVaultSettingsReader()
    val picker = FakeAttachmentPicker()
    val clipboard = FakeClipboardReader()
    val youTube = FakeYouTubeDetails()
    val opener = FakeFileOpener()
    val times = FakeNoteTimesReader()
    private val dispatchers = FakeAppDispatchers(
        dispatcher = dispatcher,
        io = io
    )

    fun create(): EditorViewModel = EditorViewModel(
        vaultPath = VAULT,
        openNote = OpenNoteUseCase(noteContentStore = store),
        saveNote = SaveNoteUseCase(noteContentStore = store),
        observeNoteOnDisk = ObserveNoteOnDiskUseCase(
            noteScanner = scanner,
            noteContentStore = store
        ),
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
        editorPreferences = preferences,
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
            youTubeDetails = youTube,
            dispatchers = dispatchers
        ),
        fillTemplate = fillTemplate(),
        propertyTools = propertyTools(),
        keepUnsavedText = KeepUnsavedTextUseCase(
            noteFileOperations = operations,
            noteContentStore = store
        ),
        noteInfoTools = noteInfoTools()
    )

    private fun noteInfoTools(): NoteInfoTools = NoteInfoTools(
        getNoteStats = GetNoteStatsUseCase(linkFinder = JetBrainsMarkdownLinkFinder()),
        noteTimesReader = times
    )

    private fun propertyTools(): PropertyTools = PropertyTools(
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
