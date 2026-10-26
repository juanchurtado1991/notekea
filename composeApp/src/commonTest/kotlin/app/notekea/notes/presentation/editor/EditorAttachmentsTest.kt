package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
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
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The open note's attachments (`ROADMAP.md` 1.8) over real use cases, an in-memory vault and a real PNG. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalEncodingApi::class)
class EditorAttachmentsTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(dispatcher = testDispatcher)
    private val viewModel by lazy { fixture.create() }
    private val png = Base64.decode(source = PNG_BASE64)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        fixture.attachmentStore.contents[RelativePath(value = "attachments/plate.png")] = png
        fixture.attachmentStore.contents[RelativePath(value = "Recipes/menu.pdf")] = PDF
        fixture.attachmentStore.contents[RelativePath(value = "attachments/broken.png")] = PDF
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `embedded files are found, images read, other files shown as files and missing ones as missing`() =
        runAttachmentsTest {
            open(text = "![[plate.png|300]]\nSee ![[menu.pdf]].\n![[broken.png]]\n![[gone.png]]\n")

            assertEquals(
                expected = listOf(
                    listOf("attachments/plate.png", AttachmentKind.IMAGE, true, false),
                    listOf("Recipes/menu.pdf", AttachmentKind.OTHER, false, false),
                    listOf("attachments/broken.png", AttachmentKind.IMAGE, false, true),
                    listOf(null, AttachmentKind.IMAGE, false, false)
                ),
                actual = viewModel.state.value.attachments.map {
                    listOf(it.path?.value, it.kind, it.isPicture, it.isUnreadable)
                }
            )
            assertEquals(expected = 300, actual = viewModel.state.value.attachments.first().embed.width)
        }

    @Test
    fun `a pasted image is saved as Obsidian names it and embedded on its own line, pasted text goes in as text`() =
        runAttachmentsTest {
            open(text = "Intro")
            viewModel.buffer.edit { selection = TextRange(index = length) }

            fixture.clipboard.image = png
            send(intent = EditorIntent.OnPaste)
            fixture.clipboard.image = null
            fixture.clipboard.text = "More"
            send(intent = EditorIntent.OnPaste)

            assertEquals(
                expected = "Intro\n\n![[Pasted image 20261026143005.png]]\nMore",
                actual = viewModel.buffer.text.toString()
            )
            assertEquals(
                expected = true,
                actual =
                RelativePath(value = "attachments/Pasted image 20261026143005.png") in
                    fixture.attachmentStore.contents
            )
        }

    @Test
    fun `dropped and picked files are copied in and embedded, a cancelled pick changes nothing`() = runAttachmentsTest {
        open(text = "")
        fixture.attachmentStore.outside[OUTSIDE] = PDF

        send(intent = EditorIntent.OnFilesDrop(paths = listOf(OUTSIDE, "$VAULT/attachments/plate.png")))
        send(intent = EditorIntent.OnAttachmentPick)
        fixture.picker.picked = OUTSIDE
        viewModel.buffer.edit { selection = TextRange(index = length) }
        send(intent = EditorIntent.OnAttachmentPick)

        assertEquals(
            expected = "![[recipe.pdf]]\n![[plate.png]]\n\n![[recipe 1.pdf]]\n",
            actual = viewModel.buffer.text.toString()
        )
    }

    @Test
    fun `a file opens with the system's app, and a failure is shown until dismissed`() = runAttachmentsTest {
        open(text = "![[menu.pdf]]")
        val menu = RelativePath(value = "Recipes/menu.pdf")

        send(intent = EditorIntent.OnAttachmentOpen(path = menu))
        fixture.opener.failure = NotesError.OpenFailed(path = menu.value)
        send(intent = EditorIntent.OnAttachmentOpen(path = menu))
        val error = viewModel.state.value.attachmentError
        send(intent = EditorIntent.OnAttachmentErrorDismiss)

        assertEquals(
            expected = Triple(listOf(menu), NotesError.OpenFailed(path = menu.value), null),
            actual = Triple(fixture.opener.opened.toList(), error, viewModel.state.value.attachmentError)
        )
    }

    @Test
    fun `locate takes the missing embed's place with the chosen file, copied in and keeping its width`() =
        runAttachmentsTest {
            open(text = "Before ![[gone.png|250]] after")
            fixture.attachmentStore.outside["/home/ana/Pictures/found.png"] = png
            fixture.picker.picked = "/home/ana/Pictures/found.png"

            send(intent = EditorIntent.OnAttachmentLocate(embed = viewModel.state.value.attachments.single().embed))

            assertEquals(
                expected = "Before ![[found.png|250]] after",
                actual = viewModel.buffer.text.toString()
            )
        }

    private fun TestScope.open(
        text: String
    ) {
        fixture.store.texts[NOTE] = text
        fixture.scanner.addNote(file = file(path = NOTE.value))
        fixture.operations.existing += NOTE
        send(intent = EditorIntent.OnOpen(path = NOTE))
    }

    /** Sends [intent], then lets the edits reach the ViewModel and the attachments be read again. */
    private fun TestScope.send(
        intent: EditorIntent
    ) {
        viewModel.onIntent(intent = intent)
        runCurrent()
        Snapshot.sendApplyNotifications()
        advanceTimeBy(delayTimeMillis = SETTLE_MILLIS)
        runCurrent()
    }

    private fun runAttachmentsTest(
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
        private const val PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
        private const val SETTLE_MILLIS = 400L
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
        private val PDF = byteArrayOf(37, 80, 68, 70)
    }
}
