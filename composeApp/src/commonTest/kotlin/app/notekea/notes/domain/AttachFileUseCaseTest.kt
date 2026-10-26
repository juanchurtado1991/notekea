package app.notekea.notes.domain

import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.FakeLocalTimeFormatter
import app.notekea.core.domain.Outcome
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class AttachFileUseCaseTest {
    private val fileOperations = FakeNoteFileOperations()
    private val store = FakeAttachmentStore(fileOperations = fileOperations)
    private val settings = FakeVaultSettingsReader()
    private val attachFile = AttachFileUseCase(
        attachmentStore = store,
        vaultSettingsReader = settings,
        noteFileOperations = fileOperations,
        localTimeFormatter = FakeLocalTimeFormatter(),
        clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T14:30:05Z"))
    )

    @Test
    fun `a pasted image is saved in the attachments folder as Obsidian names it, numbered when the name is taken`() =
        runTest {
            val first = attachFile(vaultPath = VAULT, note = NOTE, source = attachFile.pastedImage(bytes = PNG))
            val second = attachFile(vaultPath = VAULT, note = NOTE, source = attachFile.pastedImage(bytes = PNG))

            assertEquals(
                expected = listOf(
                    "attachments/Pasted image 20261026143005.png",
                    "attachments/Pasted image 20261026143005 1.png"
                ),
                actual = listOf(first, second).map { (it as Outcome.Success).value.value }
            )
        }

    @Test
    fun `an outside file is copied next to the note when Obsidian says so, one inside is linked in place`() = runTest {
        settings.attachmentFolder = "./"
        store.outside["/home/ana/Downloads/menu.pdf"] = PNG

        val copied = attachFile(
            vaultPath = VAULT,
            note = NOTE,
            source = AttachmentSource.File(absolutePath = "/home/ana/Downloads/menu.pdf")
        )
        val inside = attachFile(
            vaultPath = VAULT,
            note = NOTE,
            source = AttachmentSource.File(absolutePath = "$VAULT/Travel/lisbon.jpg")
        )

        assertEquals(
            expected = listOf("Recipes/menu.pdf", "Travel/lisbon.jpg"),
            actual = listOf(copied, inside).map { (it as Outcome.Success).value.value }
        )
        assertEquals(expected = setOf(RelativePath(value = "Recipes/menu.pdf")), actual = store.contents.keys)
    }

    companion object {
        private const val VAULT = "/home/ana/Notes"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
        private val PNG = byteArrayOf(1, 2, 3)
    }
}
