package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.viewModelScope
import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.data.OkioNoteContentStore
import app.notekea.notes.data.OkioNoteFileOperations
import app.notekea.notes.data.OkioNoteScanner
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.RenameAndUpdateLinksUseCase
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.SaveNoteUseCase
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
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

/**
 * The editor over the real disk adapters, on a real file: what unit tests with fakes cannot prove (the autosave once
 * passed with a fake store and never wrote in the app). Time is real, so each step waits for its result.
 */
@OptIn(ExperimentalCoroutinesApi::class, kotlin.io.path.ExperimentalPathApi::class)
class EditorOnDiskTest {
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
        awaitUntil { viewModel.buffer.text.isNotEmpty() }

        runBlocking(context = main) {
            viewModel.buffer.edit { append(text = "Added line.\n") }
            Snapshot.sendApplyNotifications()
        }
        awaitUntil { viewModel.state.value.saveStatus == SaveStatus.SAVED }
        runBlocking(context = main) { viewModel.viewModelScope.cancel() }

        assertEquals(
            expected = WINDOWS_NOTE + "Added line.\r\n",
            actual = note.readText()
        )
    }

    private fun editor(): EditorViewModel {
        Dispatchers.setMain(dispatcher = main)
        val dispatchers = JvmAppDispatchers()
        val store = OkioNoteContentStore(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)
        val scanner = OkioNoteScanner(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers)
        return EditorViewModel(
            vaultPath = vault.toString(),
            openNote = OpenNoteUseCase(noteContentStore = store),
            saveNote = SaveNoteUseCase(noteContentStore = store),
            observeNoteOnDisk = ObserveNoteOnDiskUseCase(noteScanner = scanner, noteContentStore = store),
            renameAndUpdateLinks = RenameAndUpdateLinksUseCase(
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
        )
    }

    private fun awaitUntil(
        condition: () -> Boolean
    ) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "Timed out waiting for the editor" }
            Thread.sleep(POLL_MILLIS)
        }
    }

    companion object {
        private const val NOTE = "Windows note.md"
        private const val POLL_MILLIS = 20L
        private const val TEMP_PREFIX = "notekea-editor"
        private const val TIMEOUT_MILLIS = 5_000L
        private const val WINDOWS_NOTE = "---\r\ntitle: Written on Windows\r\ntags: [crlf]\r\n---\r\n" +
            "# Windows line endings\r\n\r\nEvery line ends with CR LF.\r\n"
    }
}
