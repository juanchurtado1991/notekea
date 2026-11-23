package app.notekea.settings.presentation.memory

import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.memory.domain.MemoryField
import app.notekea.core.memory.domain.MemoryNote
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

/** Settings → Chat memory reads and writes `Notekea/Memory.md` (`docs/agent-memory.md`). */
@OptIn(ExperimentalCoroutinesApi::class)
class MemorySettingsViewModelTest : ViewModelTestBase() {
    private val store = FakeNoteContentStore()
    private val files = FakeNoteFileOperations()
    private val preferences = FakeAppPreferencesStore()

    private val note = """
        ## People and names
        - "Laura" is [[Laura Castro]] <!-- 2026-11-15, chat: you chose -->

        ## About me
        - I live in Costa Rica <!-- 2026-11-15, settings -->
    """.trimIndent() + "\n"

    private fun viewModelOf() = MemorySettingsViewModel(
        noteContentStore = store,
        noteFileOperations = files,
        attachmentStore = FakeAttachmentStore(),
        preferencesStore = preferences,
        localCalendar = FakeLocalCalendar(),
        clock = object : Clock {
            override fun now(): Instant = Instant.fromEpochSeconds(epochSeconds = 1_794_700_800)
        },
        vaultPath = VAULT
    )

    @Test
    fun `the memory shows as fields and learnt lines, a field typed is written once typing pauses`() =
        runTest(testDispatcher) {
            store.texts[MemoryNote.PATH] = note
            val viewModel = viewModelOf()
            advanceUntilIdle()
            val shown = viewModel.state.value

            viewModel.onIntent(intent = MemorySettingsIntent.OnFieldChange(MemoryField.COUNTRY, "Colom"))
            viewModel.onIntent(intent = MemorySettingsIntent.OnFieldChange(MemoryField.COUNTRY, "Colombia"))
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(
                    "Costa Rica",
                    listOf("\"Laura\" is [[Laura Castro]]"),
                    true,
                    false
                ),
                actual = listOf(
                    shown.valueOf(field = MemoryField.COUNTRY),
                    shown.lines.map { it.text },
                    "- I live in Colombia <!--" in store.texts.getValue(MemoryNote.PATH),
                    "Costa Rica" in store.texts.getValue(MemoryNote.PATH)
                )
            )
        }

    @Test
    fun `a learnt line is forgotten, the switch is saved, and Forget everything moves the note to the trash`() =
        runTest(testDispatcher) {
            store.texts[MemoryNote.PATH] = note
            val viewModel = viewModelOf()
            advanceUntilIdle()

            viewModel.onIntent(intent = MemorySettingsIntent.OnForget(text = "\"Laura\" is [[Laura Castro]]"))
            viewModel.onIntent(intent = MemorySettingsIntent.OnToggle)
            advanceUntilIdle()
            val forgotten = store.texts.getValue(MemoryNote.PATH)

            viewModel.onIntent(intent = MemorySettingsIntent.OnForgetAll)
            viewModel.onIntent(intent = MemorySettingsIntent.OnForgetAllConfirm)
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(false, false, MemoryNote.PATH),
                actual = listOf(
                    "Laura" in forgotten,
                    preferences.saved.chatMemory,
                    files
                        .moves
                        .single()
                        .first
                )
            )
        }

    private companion object {
        const val VAULT = "/home/ana/Notes"
    }
}
