package app.notekea.editor.presentation

import androidx.compose.runtime.snapshots.Snapshot
import app.notekea.editor.presentation.model.EditorIntent
import androidx.lifecycle.viewModelScope
import app.notekea.core.notes.domain.NoteFixtures.file
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Another note opening while a paste, a picker or a copy runs: what they bring never lands in it. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorFilesNoteChangeTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(
        dispatcher = testDispatcher,
        io = StandardTestDispatcher(scheduler = testDispatcher.scheduler)
    )
    private val viewModel by lazy { fixture.create() }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        putOnDisk(path = FIRST, text = "![[gone.png]]\n")
        putOnDisk(path = SECOND, text = "![[gone.png]]\n")
        fixture.attachmentStore.outside[OUTSIDE] = BYTES
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `text pasted while another note opens is dropped`() = runFilesTest {
        open(path = FIRST)
        fixture.clipboard.text = "pasted"

        viewModel.onIntent(intent = EditorIntent.OnPaste)
        open(path = SECOND)
        settle()

        assertEquals(expected = "![[gone.png]]\n", actual = viewModel.buffer.text.toString())
    }

    @Test
    fun `an image pasted while another note opens is not embedded in it`() = runFilesTest {
        open(path = FIRST)
        fixture.clipboard.image = BYTES

        viewModel.onIntent(intent = EditorIntent.OnPaste)
        open(path = SECOND)
        settle()

        assertEquals(expected = "![[gone.png]]\n", actual = viewModel.buffer.text.toString())
    }

    @Test
    fun `a file dropped and copied while another note opens stays attached, with no embed in that note`() =
        runFilesTest {
            open(path = FIRST)
            val copied = CompletableDeferred<Unit>()
            fixture.attachmentStore.copied = copied

            viewModel.onIntent(intent = EditorIntent.OnFilesDrop(paths = listOf(OUTSIDE)))
            runCurrent()
            open(path = SECOND)
            copied.complete(value = Unit)
            settle()

            assertEquals(expected = "![[gone.png]]\n", actual = viewModel.buffer.text.toString())
            assertEquals(expected = 1, actual = fixture.attachmentStore.contents.size)
        }

    @Test
    fun `a file picked while another note opens is not embedded in it`() = runFilesTest {
        open(path = FIRST)
        val closes = CompletableDeferred<Unit>()
        fixture.picker.closes = closes
        fixture.picker.picked = OUTSIDE

        viewModel.onIntent(intent = EditorIntent.OnAttachmentPick)
        runCurrent()
        open(path = SECOND)
        closes.complete(value = Unit)
        settle()

        assertEquals(expected = "![[gone.png]]\n", actual = viewModel.buffer.text.toString())
    }

    @Test
    fun `a file located while another note opens does not replace the same embed there`() = runFilesTest {
        open(path = FIRST)
        settle()
        val closes = CompletableDeferred<Unit>()
        fixture.picker.closes = closes
        fixture.picker.picked = OUTSIDE
        val embed = viewModel.state.value.attachments.single().embed

        viewModel.onIntent(intent = EditorIntent.OnAttachmentLocate(embed = embed))
        runCurrent()
        open(path = SECOND)
        closes.complete(value = Unit)
        settle()

        assertEquals(expected = "![[gone.png]]\n", actual = viewModel.buffer.text.toString())
    }

    private fun TestScope.open(
        path: RelativePath
    ) {
        viewModel.onIntent(intent = EditorIntent.OnOpen(path = path))
        runCurrent()
    }

    /** Lets everything still running end, and the edits reach the ViewModel. */
    private fun TestScope.settle() {
        runCurrent()
        Snapshot.sendApplyNotifications()
        testScheduler.advanceTimeBy(delayTimeMillis = SETTLE_MILLIS)
        runCurrent()
    }

    private fun putOnDisk(
        path: RelativePath,
        text: String
    ) {
        fixture.store.texts[path] = text
        fixture.scanner.addNote(file = file(path = path.value))
        fixture.operations.existing += path
    }

    private fun runFilesTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    companion object {
        private const val OUTSIDE = "/home/ana/Downloads/recipe.pdf"
        private const val SETTLE_MILLIS = 200L
        private val BYTES = byteArrayOf(1, 2, 3)
        private val FIRST = RelativePath(value = "First.md")
        private val SECOND = RelativePath(value = "Second.md")
    }
}
