package app.notekea.editor.presentation.viewmodel

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.data.NioNoteFileAttributes
import app.notekea.core.notes.data.ObsidianVaultSettingsReader
import app.notekea.core.notes.data.OkioAttachmentStore
import app.notekea.core.notes.data.OkioNoteContentStore
import app.notekea.core.notes.data.OkioNoteFileOperations
import app.notekea.core.notes.data.OkioNoteScanner
import app.notekea.core.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.core.notes.domain.FakeFileOpener
import app.notekea.core.notes.domain.FillTemplateUseCase
import app.notekea.core.notes.domain.FrontmatterContent
import app.notekea.core.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.core.notes.domain.PropertyValue
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.RenameEntryUseCase
import app.notekea.core.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.editor.data.JvmLocalTimeFormatter
import app.notekea.editor.data.OkioNoteTimesReader
import app.notekea.editor.data.SettingsEditorPreferencesStore
import app.notekea.editor.domain.AttachFileUseCase
import app.notekea.editor.domain.FakeAttachmentPicker
import app.notekea.editor.domain.FakeClipboardReader
import app.notekea.editor.domain.FakeVideoCompressor
import app.notekea.editor.domain.FakeYouTubeDetails
import app.notekea.editor.domain.GetNoteStatsUseCase
import app.notekea.editor.domain.KeepUnsavedTextUseCase
import app.notekea.editor.domain.ObserveNoteOnDiskUseCase
import app.notekea.editor.domain.OpenNoteUseCase
import app.notekea.editor.domain.RenameAndUpdateLinksUseCase
import app.notekea.editor.domain.SaveNoteUseCase
import app.notekea.editor.presentation.attachments.AttachmentTools
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.PropertyIntent
import app.notekea.editor.presentation.model.SaveStatus
import app.notekea.editor.presentation.noteinfo.NoteInfoTools
import app.notekea.editor.presentation.properties.PropertyTools
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import kotlin.io.path.deleteRecursively
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The editor over the real disk adapters, on a real file: what unit tests with fakes cannot prove (the autosave once
 * passed with a fake store and never wrote in the app). Time is real, so each step waits for its result.
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlin.io.path.ExperimentalPathApi::class)
class EditorOnDiskTest {
    private val picker = FakeAttachmentPicker()
    private val clipboard = FakeClipboardReader()
    private val main = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val vault: Path = Files.createTempDirectory(TEMP_PREFIX)

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        main.close()
        vault.deleteRecursively()
    }

    @Test
    fun `typing autosaves a note with windows line breaks, keeping them and its frontmatter byte for byte`() {
        val note = vault.resolve(NOTE).apply { writeText(text = WINDOWS_NOTE) }

        val viewModel = editor()

        runBlocking(context = main) {
            viewModel.onIntent(intent = EditorIntent.OnOpen(path = RelativePath(value = NOTE)))
        }

        awaitUntil {
            viewModel
                .buffer
                .text
                .isNotEmpty()
        }

        runBlocking(context = main) {
            viewModel.buffer.edit { append(text = "Added line.\n") }

            Snapshot.sendApplyNotifications()
        }

        awaitUntil {
            viewModel.state.value.saveStatus == SaveStatus.SAVED
        }

        runBlocking(context = main) { viewModel.viewModelScope.cancel() }

        assertEquals(
            expected = WINDOWS_NOTE + "Added line.\r\n",
            actual = note.readText()
        )
    }

    @Test
    fun `a property edited in the form rewrites only its line, the windows note otherwise byte for byte`() {
        // The frontmatter is patched, never encoded again as a whole.
        val note = vault.resolve(NOTE).apply { writeText(text = WINDOWS_NOTE) }

        val viewModel = editor()

        runBlocking(context = main) {
            viewModel.onIntent(intent = EditorIntent.OnOpen(path = RelativePath(value = NOTE)))
        }

        awaitUntil {
            viewModel.state.value.properties != FrontmatterContent.None
        }

        val key = (viewModel.state.value.properties as FrontmatterContent.Properties)
            .properties
            .first()
            .key

        runBlocking(context = main) {
            viewModel.onIntent(
                intent = PropertyIntent.OnValueChange(
                    key = key,
                    value = PropertyValue.Text(text = EDITED)
                )
            )
        }

        awaitUntil {
            note.readText() != WINDOWS_NOTE
        }

        runBlocking(context = main) { viewModel.viewModelScope.cancel() }

        assertEquals(
            expected = WINDOWS_NOTE.replace(
                oldValue = "$key: Written on Windows",
                newValue = "$key: $EDITED"
            ),
            actual = note.readText()
        )
    }

    @Test
    fun `a note on disk opens with the outline of its body, which follows a heading typed into it`() {
        vault.resolve(NOTE).writeText(text = WINDOWS_NOTE)
        val viewModel = editor()

        runBlocking(context = main) {
            viewModel.onIntent(intent = EditorIntent.OnOpen(path = RelativePath(value = NOTE)))
        }

        awaitUntil {
            viewModel
                .state
                .value
                .outline
                .isNotEmpty()
        }

        val opened = viewModel
            .state
            .value
            .outline
            .map {
                it.text to it.offset
            }

        runBlocking(context = main) {
            viewModel.buffer.edit { append(text = "## Added heading\n") }

            Snapshot.sendApplyNotifications()
        }

        awaitUntil {
            viewModel.state.value.outline.size == 2
        }

        runBlocking(context = main) { viewModel.viewModelScope.cancel() }

        assertEquals(
            expected =
            listOf("Windows line endings" to 0) to
                listOf("Windows line endings", "Added heading"),
            actual = opened to viewModel
                .state
                .value
                .outline
                .map { it.text }
        )
    }

    @Test
    fun `a file dropped from outside the vault is copied into attachments and embedded, then shown as found`() {
        vault.resolve(NOTE).writeText(text = "Intro\n")

        val outside = Files.createTempFile(TEMP_PREFIX, ".pdf").apply { writeText(text = "%PDF") }

        val viewModel = editor()

        runBlocking(context = main) {
            viewModel.onIntent(intent = EditorIntent.OnOpen(path = RelativePath(value = NOTE)))
        }

        awaitUntil {
            viewModel
                .buffer
                .text
                .isNotEmpty()
        }

        runBlocking(context = main) {
            viewModel.buffer.edit { selection = TextRange(index = length) }

            viewModel.onIntent(
                intent = EditorIntent.OnFilesDrop(paths = listOf(outside.toString()))
            )
        }

        awaitUntil {
            viewModel
                .state
                .value
                .attachments
                .singleOrNull()
                ?.path != null
        }

        runBlocking(context = main) { viewModel.viewModelScope.cancel() }

        val copied = "attachments/" + outside.fileName

        assertEquals(
            expected = Triple("Intro\n\n![[${outside.fileName}]]\n\n", copied, "%PDF"),
            actual = Triple(
                viewModel
                    .buffer
                    .text
                    .toString(),
                viewModel
                    .state
                    .value
                    .attachments
                    .single()
                    .path
                    ?.value,
                vault.resolve(copied).readText()
            )
        )

        Files.delete(outside)
    }

    /** The real attachment adapters on the temporary vault; the system's picker and clipboard are fakes. */
    private fun attachmentTools(dispatchers: JvmAppDispatchers): AttachmentTools {
        val attachmentStore = OkioAttachmentStore(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = dispatchers
        )

        val settings = ObsidianVaultSettingsReader(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = dispatchers
        )

        return AttachmentTools(
            attachmentStore = attachmentStore,
            vaultSettingsReader = settings,
            attachFile = AttachFileUseCase(
                attachmentStore = attachmentStore,
                vaultSettingsReader = settings,
                noteFileOperations = OkioNoteFileOperations(
                    fileSystem = FileSystem.SYSTEM,
                    dispatchers = dispatchers
                ),
                localTimeFormatter = JvmLocalTimeFormatter(),
                clock = Clock.System
            ),
            fileOpener = FakeFileOpener(),
            attachmentPicker = picker,
            clipboardReader = clipboard,
            youTubeDetails = FakeYouTubeDetails(),
            videoCompressor = FakeVideoCompressor(),
            dispatchers = dispatchers
        )
    }

    private fun editor(): EditorViewModel {
        Dispatchers.setMain(dispatcher = main)
        val dispatchers = JvmAppDispatchers()

        val store = OkioNoteContentStore(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = dispatchers,
            attributes = NioNoteFileAttributes()
        )

        val scanner = OkioNoteScanner(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = dispatchers
        )

        val preferences = SettingsEditorPreferencesStore(
            settings = MapSettings(),
            dispatchers = dispatchers
        )

        return EditorViewModel(
            vaultPath = vault.toString(),
            openNote = OpenNoteUseCase(noteContentStore = store),
            saveNote = SaveNoteUseCase(noteContentStore = store),
            observeNoteOnDisk = ObserveNoteOnDiskUseCase(
                noteScanner = scanner,
                noteContentStore = store
            ),
            renameAndUpdateLinks = renameAndUpdateLinks(
                store = store,
                scanner = scanner,
                dispatchers = dispatchers
            ),
            editorPreferences = preferences,
            attachmentTools = attachmentTools(dispatchers = dispatchers),
            fillTemplate = fillTemplate(
                store = store,
                dispatchers = dispatchers
            ),
            propertyTools = propertyTools(),
            keepUnsavedText = KeepUnsavedTextUseCase(
                noteFileOperations = OkioNoteFileOperations(
                    fileSystem = FileSystem.SYSTEM,
                    dispatchers = dispatchers
                ),
                noteContentStore = store
            ),
            noteInfoTools = NoteInfoTools(
                getNoteStats = GetNoteStatsUseCase(linkFinder = JetBrainsMarkdownLinkFinder()),
                noteTimesReader = OkioNoteTimesReader(
                    fileSystem = FileSystem.SYSTEM,
                    dispatchers = dispatchers
                )
            )
        )
    }

    private fun propertyTools(): PropertyTools = PropertyTools(
        localCalendar = FakeLocalCalendar(),
        clock = Clock.System
    )

    private fun renameAndUpdateLinks(
        store: OkioNoteContentStore,
        scanner: OkioNoteScanner,
        dispatchers: JvmAppDispatchers
    ): RenameAndUpdateLinksUseCase = RenameAndUpdateLinksUseCase(
        noteScanner = scanner,
        renameEntry = RenameEntryUseCase(
            noteFileOperations = OkioNoteFileOperations(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = dispatchers
            ),
            validateFileName = ValidateFileNameUseCase()
        ),
        updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
            planLinkUpdates = PlanLinkUpdatesUseCase(
                noteContentStore = store,
                linkFinder = JetBrainsMarkdownLinkFinder()
            ),
            applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)
        )
    )

    private fun fillTemplate(
        store: OkioNoteContentStore,
        dispatchers: JvmAppDispatchers
    ): FillTemplateUseCase = FillTemplateUseCase(
        vaultSettingsReader = ObsidianVaultSettingsReader(
            fileSystem = FileSystem.SYSTEM,
            dispatchers = dispatchers
        ),
        noteContentStore = store,
        localCalendar = FakeLocalCalendar(),
        clock = Clock.System
    )

    private fun awaitUntil(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS

        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "Timed out waiting for the editor" }

            // Edits the ViewModel makes reach its own text observer once the snapshot is applied, as a frame does.
            runBlocking(context = main) { Snapshot.sendApplyNotifications() }

            Thread.sleep(POLL_MILLIS)
        }
    }

    companion object {
        private const val EDITED = "Tested"
        private const val NOTE = "Windows note.md"
        private const val POLL_MILLIS = 20L
        private const val TEMP_PREFIX = "notekea-editor"
        private const val TIMEOUT_MILLIS = 5_000L
        private const val WINDOWS_NOTE =
            "---\r\ntitle: Written on Windows\r\ntags: [crlf]\r\n---\r\n" +
                "# Windows line endings\r\n\r\nEvery line ends with CR LF.\r\n"
    }
}
