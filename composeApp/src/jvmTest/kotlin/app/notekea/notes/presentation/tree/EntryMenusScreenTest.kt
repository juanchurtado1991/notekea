package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.domain.time.ElapsedTime
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.rightClickText
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.FolderCard
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.OutlineSection
import app.notekea.notes.presentation.editor.SidePanelActions
import app.notekea.notes.presentation.workspace.FolderGrid
import app.notekea.vault.domain.RecentVaultStatus
import app.notekea.vault.domain.Vault
import app.notekea.vault.presentation.welcome.RecentVaultRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What a right click offers beyond the tree: a folder grid's card the tree's own menu, an outline heading Go to heading,
 * and a recent vault Open and Remove.
 */
class EntryMenusScreenTest {
    private var frame = 0L

    @Test
    fun `a folder card's menu is the tree's, without New note, and its actions name the card`() {
        val intents = mutableListOf<VaultTreeIntent>()
        var shown = emptyList<String>()
        scene(content = {
            val state = TreePreviewData.state()
            FolderGrid(
                folder = RelativePath(value = FOLDER),
                fileCount = 1,
                cards = listOf(FolderCard(path = CARD, kind = FolderCard.Kind.NOTE)),
                covers = emptyMap(),
                onOpen = {},
                onCoverNeeded = { _, _ -> },
                cardMenu = { card, content ->
                    EntryMenuArea(row = card.asTreeRow(), state = state, onIntent = {
                        intents += it
                    }, content = content)
                }
            )
        }) { scene ->
            scene.rightClickText(text = CARD.nameWithoutExtension) { settle(scene = scene) }
            shown = scene.texts()
            scene.clickText(text = "Rename") { settle(scene = scene) }
        }

        assertEquals(
            expected = listOf<VaultTreeIntent>(VaultTreeIntent.OnRenameRequest(path = CARD)),
            actual = intents
        )
        assertTrue(actual = "Delete note" in shown && "New note" !in shown, message = shown.toString())
    }

    @Test
    fun `an outline heading's menu goes to it`() {
        val chosen = mutableListOf<Int>()
        scene(content = {
            OutlineSection(
                outline = OutlineFinder().headings(text = "# First\n\n## Second\n"),
                current = 0,
                focusRequester = remember { FocusRequester() },
                actions = SidePanelActions(
                    onHeadingClick = { chosen += it },
                    onModeSelect = {},
                    onSectionToggle = {},
                    onHide = {},
                    onLeave = {}
                )
            )
        }) { scene ->
            scene.rightClickText(text = "Second") { settle(scene = scene) }
            scene.clickText(text = "Go to heading") { settle(scene = scene) }
        }

        assertEquals(expected = listOf(1), actual = chosen)
    }

    @Test
    fun `a recent vault's menu opens or removes it, and one whose folder is gone offers Locate`() {
        val sent = mutableListOf<String>()
        var locate = emptyList<String>()
        scene(content = { RecentRow(isMissing = false, sent = sent) }) { scene ->
            scene.rightClickText(text = VAULT) { settle(scene = scene) }
            scene.clickText(text = "Remove") { repeat(times = MENU_CLOSE_SETTLES) { settle(scene = scene) } }
            scene.rightClickText(text = VAULT) { settle(scene = scene) }
            scene.clickText(text = "Open vault") { settle(scene = scene) }
        }
        scene(content = { RecentRow(isMissing = true, sent = mutableListOf()) }) { scene ->
            scene.rightClickText(text = VAULT) { settle(scene = scene) }
            locate = scene.texts()
        }

        assertEquals(expected = listOf("remove", "open"), actual = sent)
        assertTrue(actual = locate.count { it == "Locate…" } == 2, message = locate.toString())
    }

    @Composable
    private fun RecentRow(
        isMissing: Boolean,
        sent: MutableList<String>
    ) {
        RecentVaultRow(
            recentVault = RecentVaultStatus(
                vault = Vault.fromPath(path = "/home/ana/$VAULT"),
                lastOpened = ElapsedTime.JustNow,
                isMissing = isMissing
            ),
            platformConventions = PreviewPlatformConventions.linux,
            onOpen = { sent += "open" },
            onLocate = { sent += "locate" },
            onRemove = { sent += "remove" }
        )
    }

    private fun scene(
        content: @Composable () -> Unit,
        drive: (ImageComposeScene) -> Unit
    ) {
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) { Box(modifier = Modifier.fillMaxSize()) { content() } }
            }.use { scene ->
                settle(scene = scene)
                drive(scene)
            }
        }
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val FOLDER = "Recipes"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 700
        private const val MENU_CLOSE_SETTLES = 10
        private const val SETTLE_FRAMES = 5
        private const val VAULT = "Notes"
        private const val WIDTH = 800
        private val CARD = RelativePath(value = "Recipes/Arepas.md")
    }
}
