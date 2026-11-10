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

/** Files pasted, dropped, picked, opened and located from the open note, over an in-memory vault. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorNewAttachmentsTest : EditorAttachmentsTestBase() {
    @Test
    fun `text pasted with windows line breaks goes in with plain ones, so a windows note saves them once`() =
        runAttachmentsTest {
            open(text = "Intro\r\n")

            viewModel.buffer.edit { selection = TextRange(index = length) }

            fixture.clipboard.text = "One\r\nTwo\r\n"

            send(intent = EditorIntent.OnPaste)
            send(intent = EditorIntent.OnSaveShortcut)

            assertEquals(
                expected = "Intro\nOne\nTwo\n",
                actual = viewModel
                    .buffer
                    .text
                    .toString()
            )

            assertEquals(
                expected = "Intro\r\nOne\r\nTwo\r\n",
                actual = fixture.store.texts[NOTE]
            )
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
                expected = "Intro\n\n![[Pasted image 20261026143005.png|1]]\n\nMore",
                actual = viewModel
                    .buffer
                    .text
                    .toString()
            )

            assertEquals(
                expected = true,
                actual =
                RelativePath(value = "attachments/Pasted image 20261026143005.png") in
                    fixture.attachmentStore.contents
            )
        }

    @Test
    fun `a new image gets a width to see and change, at most 400, and other files none`() =
        runAttachmentsTest {
            open(text = "")
            fixture.clipboard.image = Base64.decode(source = WIDE_PNG_BASE64)
            send(intent = EditorIntent.OnPaste)

            assertEquals(
                expected = "![[Pasted image 20261026143005.png|400]]\n\n",
                actual = viewModel
                    .buffer
                    .text
                    .toString()
            )
        }

    @Test
    fun `an image the clipboard cannot give is explained, and nothing is pasted`() =
        runAttachmentsTest {
            open(text = "Intro")
            fixture.clipboard.text = "stale text"
            fixture.clipboard.imageError = NotesError.ClipboardToolMissing(tool = "wl-clipboard")

            send(intent = EditorIntent.OnPaste)

            assertEquals(
                expected = "Intro" to NotesError.ClipboardToolMissing(tool = "wl-clipboard"),
                actual = viewModel
                    .buffer
                    .text
                    .toString() to viewModel.state.value.actionError
            )
        }

    @Test
    fun `dropped and picked files are copied in and embedded, a cancelled pick changes nothing`() =
        runAttachmentsTest {
            open(text = "")
            fixture.attachmentStore.outside[OUTSIDE] = PDF

            send(
                intent = EditorIntent.OnFilesDrop(
                    paths = listOf(OUTSIDE, "$VAULT/attachments/plate.png")
                )
            )

            send(intent = EditorIntent.OnAttachmentPick)
            fixture.picker.picked = OUTSIDE

            viewModel.buffer.edit { selection = TextRange(index = length) }

            send(intent = EditorIntent.OnAttachmentPick)

            assertEquals(
                expected = "![[recipe.pdf]]\n![[plate.png|1]]\n\n![[recipe 1.pdf]]\n\n",
                actual = viewModel
                    .buffer
                    .text
                    .toString()
            )
        }

    @Test
    fun `a file opens with the system's app, and a failure is shown until dismissed`() =
        runAttachmentsTest {
            open(text = "![[menu.pdf]]")
            val menu = RelativePath(value = "Recipes/menu.pdf")

            send(intent = EditorIntent.OnAttachmentOpen(path = menu))
            fixture.opener.failure = NotesError.OpenFailed(path = menu.value)
            send(intent = EditorIntent.OnAttachmentOpen(path = menu))
            val error = viewModel.state.value.actionError
            send(intent = EditorIntent.OnActionErrorDismiss)

            assertEquals(
                expected = Triple(listOf(menu), NotesError.OpenFailed(path = menu.value), null),
                actual = Triple(
                    fixture
                        .opener
                        .opened
                        .toList(),
                    error,
                    viewModel.state.value.actionError
                )
            )
        }

    @Test
    fun `locate takes the missing embed's place with the chosen file, copied in and keeping its width`() =
        runAttachmentsTest {
            open(text = "Before ![[gone.png|250]] after")
            fixture.attachmentStore.outside["/home/ana/Pictures/found.png"] = png
            fixture.picker.picked = "/home/ana/Pictures/found.png"

            send(
                intent = EditorIntent.OnAttachmentLocate(
                    embed = viewModel
                        .state
                        .value
                        .attachments
                        .single()
                        .embed
                )
            )

            assertEquals(
                expected = "Before ![[found.png|250]] after",
                actual = viewModel
                    .buffer
                    .text
                    .toString()
            )
        }
}
