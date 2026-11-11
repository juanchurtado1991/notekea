package app.notekea.editor.presentation.attachments

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.file
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.domain.EmbedProblem
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.viewmodel.EditorViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.io.encoding.Base64
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** An editor over an in-memory vault that holds a real PNG and a PDF, and the steps its attachment tests take. */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class EditorAttachmentsTestBase {
    protected val testDispatcher = StandardTestDispatcher()
    protected val fixture = EditorViewModels(dispatcher = testDispatcher)
    protected val viewModel by lazy { fixture.create() }
    protected val png = Base64.decode(source = PNG_BASE64)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        fixture.attachmentStore.contents[RelativePath(value = "attachments/plate.png")] = png
        fixture.attachmentStore.contents[RelativePath(value = "Recipes/menu.pdf")] = PDF
        fixture.attachmentStore.contents[RelativePath(value = "attachments/Arepas.mp4")] = PDF
        fixture.attachmentStore.contents[RelativePath(value = "attachments/broken.png")] = PDF
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    protected fun TestScope.open(text: String) {
        fixture.store.texts[NOTE] = text
        fixture.scanner.addNote(file = file(path = NOTE.value))
        fixture.operations.existing += NOTE
        send(intent = EditorIntent.OnOpen(path = NOTE))
    }

    /** Types [text] over the body, then lets the attachments be read again. */
    protected fun TestScope.type(text: String) {
        viewModel.buffer.edit {
            replace(
                start = 0,
                end = length,
                text = text
            )
        }

        Snapshot.sendApplyNotifications()
        advanceTimeBy(delayTimeMillis = SETTLE_MILLIS)
        runCurrent()
    }

    /** Sends [intent], then lets the edits reach the ViewModel and the attachments be read again. */
    protected fun TestScope.send(intent: EditorIntent) {
        viewModel.onIntent(intent = intent)
        runCurrent()
        Snapshot.sendApplyNotifications()
        advanceTimeBy(delayTimeMillis = SETTLE_MILLIS)
        runCurrent()
    }

    protected fun runAttachmentsTest(body: suspend TestScope.() -> Unit) = runTest(
        context = testDispatcher
    ) {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    protected companion object {
        const val OUTSIDE = "/home/ana/Downloads/recipe.pdf"
        const val BIG_VIDEO = "/home/ana/Videos/trip.mov"
        const val SMALL_COPY = "/home/ana/.notekea/compressing/1/trip.mp4"
        const val PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
        const val SETTLE_MILLIS = 400L
        const val VIDEO_ID = "dQw4w9WgXcQ"

        // A 500 x 2 picture, wider than the default width of a new image.
        const val WIDE_PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAfQAAAACCAIAAAAhCLltAAAAIElEQVR42u3QAREAAAQEMGQVViw9/rYI69sCIMwoAMjz4FkBMJDyS2EAAAAASUVORK5CYII="
        val NOTE = RelativePath(value = "Recipes/Arepas.md")
        val PLAIN = RelativePath(value = "Recipes/Pancakes.md")
        val PDF = byteArrayOf(37, 80, 68, 70)
    }
}
