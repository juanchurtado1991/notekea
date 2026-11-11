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
import app.notekea.editor.presentation.reading.ReadingSegment
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

/** The files and notes the open note embeds, over real use cases, an in-memory vault and a real PNG. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorAttachmentsTest : EditorAttachmentsTestBase() {
    @Test
    fun `a listing that fails keeps the last one, so the files already found stay found`() =
        runAttachmentsTest {
            open(text = "![[menu.pdf]]\n")
            fixture.attachmentStore.listingFails = true

            type(text = "![[menu.pdf]]\n![[gone.png]]\n")

            assertEquals(
                expected = listOf(RelativePath(value = "Recipes/menu.pdf"), null),
                actual = viewModel
                    .state
                    .value
                    .attachments
                    .map { it.path }
            )
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
                actual = viewModel
                    .state
                    .value
                    .attachments
                    .map { listOf(it.path?.value, it.kind, it.isPicture, it.isUnreadable) }
            )

            assertEquals(
                expected = 300,
                actual = viewModel
                    .state
                    .value
                    .attachments
                    .first()
                    .embed
                    .width
            )
        }

    // The developer's request of 2026-11-11: a video plays in its note, from where it is on the disk.
    @Test
    fun `an embedded video is found with where it is on the disk, to play, and nothing else is`() =
        runAttachmentsTest {
            open(text = "![[Arepas.mp4]]\n![[menu.pdf]]\n")

            assertEquals(
                expected = listOf(
                    listOf(AttachmentKind.VIDEO, "$VAULT/attachments/Arepas.mp4"),
                    listOf(AttachmentKind.OTHER, null)
                ),
                actual = viewModel
                    .state
                    .value
                    .attachments
                    .map { listOf(it.kind, it.location) }
            )
        }

    @Test
    fun `a YouTube video embedded with its title is a video card, a plain link or another site is not`() =
        runAttachmentsTest {
            open(
                text = "![Cómo hacer arepas](https://youtu.be/dQw4w9WgXcQ)\n" +
                    "See https://www.youtube.com/watch?v=dQw4w9WgXcQ\n![](https://example.com/video)\n"
            )

            assertEquals(
                expected = listOf(listOf(AttachmentKind.YOUTUBE, "dQw4w9WgXcQ", "Cómo hacer arepas")),
                actual = viewModel
                    .state
                    .value
                    .attachments
                    .map { listOf(it.kind, it.youTubeId, it.embed.label) }
            )
        }

    @Test
    fun `a picture stays decoded while its note is shown, and is let go once another note is`() =
        runAttachmentsTest {
            val plate = RelativePath(value = "attachments/plate.png")
            open(text = "![[plate.png]]\n")
            fixture.store.texts[PLAIN] = "No pictures here.\n"
            fixture.scanner.addNote(file = file(path = PLAIN.value))
            send(intent = EditorIntent.OnOpen(path = PLAIN))
            send(intent = EditorIntent.OnOpen(path = NOTE))

            // Read when the note opened, and again only because another note was shown in between.
            assertEquals(
                expected = 2,
                actual = fixture
                    .attachmentStore
                    .reads
                    .count {
                        it == plate
                    }
            )
        }

    @Test
    fun `an embedded note shows its text or one section, a missing note or the note itself says why`() =
        runAttachmentsTest {
            fixture.attachmentStore.contents[RelativePath(value = "Shopping.md")] =
                "---\ntags: [a]\n---\n# Shopping\n\n## Dairy\n\nCheese".encodeToByteArray()

            fixture.attachmentStore.contents[NOTE] = "".encodeToByteArray()

            open(
                text = listOf(
                    "Shopping",
                    "Gone",
                    "Arepas",
                    "Shopping#Dairy",
                    "Shopping#Fish",
                    "plate.png"
                )
                    .joinToString(separator = "") { "![[$it]]\n" }
            )

            assertEquals(
                expected = listOf(
                    Triple("Shopping", null, "# Shopping\n\n## Dairy\n\nCheese"),
                    Triple("Gone", EmbedProblem.MISSING_NOTE, null),
                    Triple("Arepas", EmbedProblem.CIRCULAR, null),
                    Triple("Shopping#Dairy", null, "## Dairy\n\nCheese"),
                    Triple("Shopping#Fish", EmbedProblem.MISSING_SECTION, null),
                    Triple("plate.png", null, null)
                ),
                actual = viewModel
                    .state
                    .value
                    .attachments
                    .map { Triple(it.embed.target, it.problem, it.noteText) }
            )

            assertEquals(
                expected = listOf(
                    "Shopping",
                    "Gone",
                    "Arepas",
                    "Shopping#Dairy",
                    "Shopping#Fish",
                    "plate.png"
                ),
                actual = ReadingSegment
                    .of(
                        text = viewModel
                            .buffer
                            .text
                            .toString(),
                        attachments = viewModel.state.value.attachments
                    )
                    .filterIsInstance<ReadingSegment.Attachment>()
                    .map { it.view.embed.target }
            )
        }

    @Test
    fun `embedded notes show their own embeds three notes deep, and stop at a circle or deeper`() =
        runAttachmentsTest {
            mapOf(
                "One.md" to "One text\n![[Two]]\n![[plate.png]]",
                "Two.md" to "Two text\n![[Three]]\n![[One]]",
                "Three.md" to "Three text\n![[Four]]",
                "Four.md" to "Four text"
            ).forEach { (name, text) ->
                fixture.attachmentStore.contents[RelativePath(value = name)] =
                    text.encodeToByteArray()
            }

            open(text = "![[One]]\n")

            val one = viewModel
                .state
                .value
                .attachments
                .single()

            val two = one.nested.first()
            val three = two.nested.first()

            assertEquals(
                expected = listOf(
                    listOf("Two" to null, "plate.png" to null),
                    listOf("Three" to null, "One" to EmbedProblem.CIRCULAR),
                    listOf("Four" to EmbedProblem.TOO_DEEP)
                ),
                actual = listOf(one, two, three).map { view ->
                    view.nested.map {
                        it.embed.target to it.problem
                    }
                }
            )
        }

    @Test
    fun `an embedded note added outside Notekea is found once the text changes`() =
        runAttachmentsTest {
            open(text = "![[Later]]\n")

            fixture.attachmentStore.contents[RelativePath(value = "Later.md")] =
                "Now here".encodeToByteArray()

            type(text = "![[Later]]\nMore\n")

            assertEquals(
                expected = "Now here" to null,
                actual = viewModel
                    .state
                    .value
                    .attachments
                    .single()
                    .run {
                        noteText to problem
                    }
            )
        }

    @Test
    fun `an embedded note moved outside Notekea is found where it went`() = runAttachmentsTest {
        val moved = RelativePath(value = "New/Later.md")

        fixture.attachmentStore.contents[RelativePath(value = "Old/Later.md")] =
            "Here".encodeToByteArray()

        open(text = "![[Later]]\n")

        fixture
            .attachmentStore
            .contents
            .remove(key = RelativePath(value = "Old/Later.md"))

        fixture.attachmentStore.contents[moved] = "Here".encodeToByteArray()

        type(text = "![[Later]]\nMore\n")

        assertEquals(
            expected = moved to null,
            actual = viewModel
                .state
                .value
                .attachments
                .single()
                .run {
                    path to problem
                }
        )
    }
}
