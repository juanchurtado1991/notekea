package app.notekea.notes.domain

import java.io.File
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The tabs cases of `MVP_SMOKE_TEST.md` (1.11) on the smoke data itself: the Playground Vault's folders show the
 * cards and pictures the guide names, so it never promises a cover the data does not give.
 */
class SmokeTabsTest {
    private val vaultFiles = File(PERSONAL_NOTES).walkTopDown()
        .filter { it.isFile }
        .map { it.relativeTo(base = File(PERSONAL_NOTES)).invariantSeparatorsPath }
        .filterNot { path -> path.split("/").any { it.startsWith(".") } }
        .map { RelativePath(value = it) }
        .toList() + File(SMOKE_ATTACHMENTS).listFiles().orEmpty().map { RelativePath(value = "attachments/" + it.name) }

    @Test
    fun `1_11-09 Images and files shows the plate on its card, and the attachments grid lists its files`() = runTest {
        val note = RelativePath(value = "Editor/Images and files.md")
        val store = FakeNoteContentStore().apply { texts[note] = File(SMOKE_NOTE).readText() }
        val cover = GetNoteCoverUseCase(noteContentStore = store, vaultSettingsReader = FakeVaultSettingsReader())(
            vaultPath = PERSONAL_NOTES,
            note = note,
            files = vaultFiles
        )
        val tree = vaultFiles.filterNot { it.isMarkdown }.fold(initial = VaultTree.EMPTY) { tree, path ->
            tree.withAttachment(file = NoteFile(path = path, size = null, modifiedAt = null))
        }

        assertEquals(
            expected = "attachments/Arepas plate.png" to listOf(
                "Arepas plate.png" to FolderCard.Kind.IMAGE,
                "broken.png" to FolderCard.Kind.IMAGE,
                "shopping.txt" to FolderCard.Kind.FILE,
                "Weekly menu.pdf" to FolderCard.Kind.FILE
            ),
            actual = cover?.value to FolderCards(naturalNameComparator = NaturalNameComparator())
                .of(folder = RelativePath(value = "attachments"), tree = tree)
                .map { it.path.name to it.kind }
        )
    }

    companion object {
        private const val PERSONAL_NOTES = "../smoke-data/vaults/Personal Notes"
        private const val SMOKE_ATTACHMENTS = "../smoke-data/attachments"
        private const val SMOKE_NOTE = "../smoke-data/notes/Images and files.md"
    }
}
