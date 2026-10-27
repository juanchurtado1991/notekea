package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFile
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreeSort
import app.notekea.notes.domain.VaultTree
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 1.8 smoke finding: the attachments folder looked empty, because the tree listed notes only. Files that are not notes
 * are rows of their own, and a selected picture shows beside the tree with Open with its app and Show in Files.
 */
class TreeFilesScreenTest {
    @Test
    fun `a file row selects the file, whose picture offers to open it or show it in the file manager`() {
        val intents = mutableListOf<VaultTreeIntent>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = true) {
                    VaultTreeContent(
                        state = state(),
                        vaultName = VAULT_NAME,
                        onIntent = { intents += it },
                        rail = SidebarRailActions()
                    ) { PlatePreview(onIntent = { intents += it }) }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                listOf(MENU_ROW, OPEN_BUTTON, REVEAL_BUTTON).forEachIndexed { index, position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = FRAME_NANOS * (index + 1))
                }
            }
        }

        assertEquals(
            expected = listOf(
                VaultTreeIntent.OnRowClick(path = MENU),
                VaultTreeIntent.OnFileOpen(path = PLATE),
                VaultTreeIntent.OnRevealRequest(path = PLATE)
            ),
            actual = intents.filter {
                it is VaultTreeIntent.OnRowClick || it is VaultTreeIntent.OnFileOpen ||
                    it is VaultTreeIntent.OnRevealRequest
            }
        )
    }

    /** The picture's tab beside the tree, as the tabs show a file (`ROADMAP.md` 1.11). */
    @Composable
    private fun PlatePreview(
        onIntent: (VaultTreeIntent) -> Unit
    ) {
        FileDetails(
            preview = FilePreview(
                file = file(path = PLATE),
                kind = AttachmentKind.IMAGE,
                image = ImageBitmap(width = IMAGE_WIDTH, height = IMAGE_HEIGHT, config = ImageBitmapConfig.Argb8888)
            ),
            platformConventions = PreviewPlatformConventions.linux,
            onIntent = onIntent
        )
    }

    private fun state(): VaultTreeState {
        val tree = VaultTree.EMPTY
            .withNote(note = NoteEntry(file = file(path = WELCOME), metadata = NoteMetadata.NONE))
            .withAttachment(file = file(path = PLATE))
            .withAttachment(file = file(path = MENU))
        val expanded = setOf(RelativePath(value = ATTACHMENTS))
        return VaultTreeState(
            vaultPath = VAULT_PATH,
            platformConventions = PreviewPlatformConventions.linux,
            isLoading = false,
            tree = tree,
            rows = FlattenTreeUseCase(naturalNameComparator = NaturalNameComparator())(
                tree = tree,
                expandedFolders = expanded,
                sort = TreeSort.NAME
            ),
            expandedFolders = expanded,
            selectedPath = PLATE
        )
    }

    private fun file(
        path: RelativePath
    ): NoteFile = NoteFile(path = path, size = null, modifiedAt = null)

    companion object {
        private const val ATTACHMENTS = "attachments"
        private const val DENSITY = 1f
        private const val FRAME_NANOS = 16_000_000L
        private const val IMAGE_HEIGHT = 240
        private const val IMAGE_WIDTH = 480
        private const val VAULT_NAME = "Playground Vault"
        private const val VAULT_PATH = "/home/ana/Playground Vault"
        private const val WINDOW_HEIGHT = 800
        private const val WINDOW_WIDTH = 1_300
        private val MENU = RelativePath(value = "attachments/Weekly menu.pdf")
        private val MENU_ROW = Offset(x = 168f, y = 126f)
        private val OPEN_BUTTON = Offset(x = 463f, y = 388f)
        private val PLATE = RelativePath(value = "attachments/Arepas plate.png")
        private val REVEAL_BUTTON = Offset(x = 623f, y = 388f)
        private val WELCOME = RelativePath(value = "Welcome.md")
    }
}
