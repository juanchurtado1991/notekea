package app.notekea.settings.presentation.files

import app.notekea.core.notes.domain.DailyNoteSettings
import app.notekea.core.notes.domain.FakeVaultSettingsReader
import app.notekea.core.notes.domain.FakeVaultSettingsWriter
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

    @Test
    fun `without a vault there is nothing to change`() = runTest(dispatcher) {
        val viewModel = FilesSettingsViewModel(
            reader = FakeVaultSettingsReader(),
            writer = FakeVaultSettingsWriter(),
            vaultPath = null
        )

        advanceUntilIdle()

        assertEquals(
            expected = false,
            actual = viewModel.state.value.hasVault
        )
    }
}
