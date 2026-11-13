package app.notekea.settings.presentation.files

import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.core.notes.domain.FakeVaultSettingsWriter
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.templates.DailyNoteSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

/** Settings → Files reads and writes the vault's `.obsidian` settings, once typing pauses (5.2, 2026-11-11). */
@OptIn(ExperimentalCoroutinesApi::class)
class FilesSettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the vault's settings show, a folder typed is written once typing pauses, a failure is said`() =
        runTest(dispatcher) {
            val writer = FakeVaultSettingsWriter()
            val attachmentStore = FakeAttachmentStore()

            val viewModel = FilesSettingsViewModel(
                reader = FakeVaultSettingsReader(
                    attachmentFolder = "Assets",
                    dailyNotes = DailyNoteSettings(
                        folder = "Journal",
                        format = "YYYY-MM-DD",
                        template = null
                    )
                ),
                writer = writer,
                attachmentStore = attachmentStore,
                localCalendar = FakeLocalCalendar(),
                clock = Clock.System,
                vaultPath = "/home/ana/Notes"
            )

            advanceUntilIdle()
            val shown = viewModel.state.value

            "files/img".forEachIndexed { index, _ ->
                viewModel.onIntent(intent = FilesSettingsIntent.OnAttachmentsChange(folder = "files/img".take(n = index + 1)))
            }

            advanceUntilIdle()
            writer.fails = true
            viewModel.onIntent(intent = FilesSettingsIntent.OnTemplatesFolderChange(folder = "Templates"))
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(
                    "Assets",
                    "Journal",
                    listOf("app.json attachmentFolderPath=files/img"),
                    true
                ),
                actual = listOf(
                    shown.attachments,
                    shown.dailyFolder,
                    writer.written,
                    viewModel.state.value.hasFailed
                )
            )
        }

    @Test
    fun `typing in one folder and then another writes both`() = runTest(dispatcher) {
        val writer = FakeVaultSettingsWriter()

        val viewModel = FilesSettingsViewModel(
            reader = FakeVaultSettingsReader(),
            writer = writer,
            attachmentStore = FakeAttachmentStore(),
            localCalendar = FakeLocalCalendar(),
            clock = Clock.System,
            vaultPath = "/home/ana/Notes"
        )

        advanceUntilIdle()
        viewModel.onIntent(intent = FilesSettingsIntent.OnAttachmentsChange(folder = "files"))
        viewModel.onIntent(intent = FilesSettingsIntent.OnTemplatesFolderChange(folder = "Templates"))
        advanceUntilIdle()

        // Before the fix the second field's pause cancelled the first one's write (bug hunt, 2026-11-11).
        assertEquals(
            expected = listOf(
                "app.json attachmentFolderPath=files",
                "templates.json folder=Templates"
            ),
            actual = writer.written
        )
    }

    // The developer's smoke of 2026-11-13: free text fields nobody understood. The folders are the vault's own, and
    // a date format shows as a date.
    @Test
    fun `the folders offered are the vault's, without the hidden ones, and each format shows a date written in it`() =
        runTest(dispatcher) {
            val attachmentStore = FakeAttachmentStore()

            listOf("Journal/Daily/2026-11-13.md", "attachments/plate.png", ".obsidian/app.json", "Welcome.md")
                .forEach { attachmentStore.contents[RelativePath(value = it)] = byteArrayOf() }

            val viewModel = FilesSettingsViewModel(
                reader = FakeVaultSettingsReader(
                    dailyNotes = DailyNoteSettings(
                        folder = "Diario",
                        format = "dddd D",
                        template = null
                    )
                ),
                writer = FakeVaultSettingsWriter(),
                attachmentStore = attachmentStore,
                localCalendar = FakeLocalCalendar(),
                clock = FixedClock,
                vaultPath = "/home/ana/Notes"
            )

            advanceUntilIdle()
            val state = viewModel.state.value

            assertEquals(
                expected = listOf<Any>(
                    listOf("Journal", "Journal/Daily", "attachments"),
                    listOf("Diario", "Journal", "Journal/Daily", "attachments"),
                    listOf("2026-11-13", "13-11-2026", "11-13-2026", "2026.11.13", "13.11.2026", "20261113", "dddd D")
                ),
                actual = listOf(
                    state.folders,
                    state.foldersWith(current = state.dailyFolder),
                    state.formats.map { it.example }
                )
            )
        }

    private object FixedClock : Clock {
        override fun now(): Instant = Instant.parse(input = "2026-11-13T12:00:00Z")
    }

    @Test
    fun `without a vault there is nothing to change`() = runTest(dispatcher) {
        val viewModel = FilesSettingsViewModel(
            reader = FakeVaultSettingsReader(),
            writer = FakeVaultSettingsWriter(),
            attachmentStore = FakeAttachmentStore(),
            localCalendar = FakeLocalCalendar(),
            clock = Clock.System,
            vaultPath = null
        )

        advanceUntilIdle()

        assertEquals(
            expected = false,
            actual = viewModel.state.value.hasVault
        )
    }
}
