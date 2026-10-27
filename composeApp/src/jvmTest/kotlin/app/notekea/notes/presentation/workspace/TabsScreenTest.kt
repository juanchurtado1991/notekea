package app.notekea.notes.presentation.workspace

import androidx.compose.material3.Text
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultTree
import app.notekea.notes.domain.Workspace
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The editor's tabs on screen (`ROADMAP.md` 1.11): a click selects a tab, its close button and the middle button close
 * it, its menu splits right, a click in a pane gives it the keyboard, and a tab dragged to the other pane, or to the
 * right edge of a single one, moves there.
 */
class TabsScreenTest {
    private val notes = listOf("Recipes/Arepas.md", "Welcome.md", "Ideas.md").map { RelativePath(value = it) }
    private val tree = notes.fold(initial = VaultTree.EMPTY) { tree, path ->
        tree.withNote(note = NoteEntry(file = file(path = path.value), metadata = NoteMetadata.NONE))
    }
    private val single = notes.fold(initial = Workspace.EMPTY) { workspace, path -> workspace.open(path = path) }
    private var frame = 0L

    @Test
    fun `a click selects a tab, its close and middle buttons close it, and its menu splits right`() {
        val intents = scene(workspace = single) { scene ->
            click(scene = scene, position = WELCOME_TAB)
            click(scene = scene, position = AREPAS_CLOSE)
            click(scene = scene, position = IDEAS_TAB, button = PointerButton.Tertiary)
            click(scene = scene, position = AREPAS_TAB, button = PointerButton.Secondary)
            click(scene = scene, position = SPLIT_RIGHT)
        }

        assertEquals(
            expected = listOf(
                WorkspaceIntent.OnTabSelect(pane = 0, tab = 1),
                WorkspaceIntent.OnTabClose(pane = 0, tab = 0),
                WorkspaceIntent.OnTabClose(pane = 0, tab = 2),
                WorkspaceIntent.OnTabMove(path = notes[0], toPane = 1, toIndex = 0)
            ),
            actual = intents.filterNot { it is WorkspaceIntent.OnPaneFocus }
        )
    }

    @Test
    fun `a click in the other pane gives it the keyboard, and a tab dragged there or to a single pane's edge moves`() {
        val split = scene(workspace = single.splitRight()) { scene ->
            click(scene = scene, position = LEFT_PANE)
            drag(scene = scene, from = AREPAS_TAB, to = RIGHT_PANE)
        }
        val edge = scene(workspace = single) { scene -> drag(scene = scene, from = AREPAS_TAB, to = RIGHT_EDGE) }

        assertEquals(
            expected = listOf(
                WorkspaceIntent.OnPaneFocus(pane = 0),
                WorkspaceIntent.OnTabMove(path = notes[0], toPane = 1, toIndex = 1)
            ) to listOf<WorkspaceIntent>(WorkspaceIntent.OnTabMove(path = notes[0], toPane = 1, toIndex = 0)),
            actual = split.filterNot { it is WorkspaceIntent.OnTabSelect }.distinct() to
                edge.filter { it is WorkspaceIntent.OnTabMove }
        )
    }

    @Test
    fun `a tab dragged along its own row takes the place where it is dropped`() {
        // 1.11 smoke: dragging a tab among the others of its pane did not reorder them.
        val intents = scene(workspace = single) { scene -> drag(scene = scene, from = AREPAS_TAB, to = BEFORE_IDEAS) }

        assertEquals(
            expected = listOf<WorkspaceIntent>(WorkspaceIntent.OnTabMove(path = notes[0], toPane = 0, toIndex = 1)),
            actual = intents.filter { it is WorkspaceIntent.OnTabMove }
        )
    }

    private fun scene(
        workspace: Workspace,
        actions: (ImageComposeScene) -> Unit
    ): List<WorkspaceIntent> {
        val intents = mutableListOf<WorkspaceIntent>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    WorkspaceContent(
                        state = WorkspaceState(workspace = workspace, tree = tree),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = { intents += it },
                        startActions = WorkspaceStartActions(),
                        unsavedPaths = emptySet()
                    ) { _, path, _ -> Text(text = path.value) }
                }
            }.use { scene ->
                settle(scene = scene)
                actions(scene)
            }
        }
        return intents
    }

    private fun click(
        scene: ImageComposeScene,
        position: Offset,
        button: PointerButton = PointerButton.Primary
    ) {
        val pressed = PointerButtons(
            isPrimaryPressed = button == PointerButton.Primary,
            isSecondaryPressed = button == PointerButton.Secondary,
            isTertiaryPressed = button == PointerButton.Tertiary
        )
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = position)
        scene.sendPointerEvent(
            eventType = PointerEventType.Press,
            position = position,
            buttons = pressed,
            button = button
        )
        settle(scene = scene)
        scene.sendPointerEvent(
            eventType = PointerEventType.Release,
            position = position,
            buttons = PointerButtons(),
            button = button
        )
        settle(scene = scene)
    }

    private fun drag(
        scene: ImageComposeScene,
        from: Offset,
        to: Offset
    ) {
        val pressed = PointerButtons(isPrimaryPressed = true)
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = from)
        scene.sendPointerEvent(
            eventType = PointerEventType.Press,
            position = from,
            buttons = pressed,
            button = PointerButton.Primary
        )
        (1..DRAG_STEPS).forEach { step ->
            val position = from + (to - from) * (step.toFloat() / DRAG_STEPS)
            scene.sendPointerEvent(eventType = PointerEventType.Move, position = position, buttons = pressed)
            settle(scene = scene)
        }
        scene.sendPointerEvent(
            eventType = PointerEventType.Release,
            position = to,
            buttons = PointerButtons(),
            button = PointerButton.Primary
        )
        // A tab let go on its row glides into its place before it moves there.
        repeat(times = GLIDE_SETTLES) { settle(scene = scene) }
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val DRAG_STEPS = 10
        private const val FRAME_NANOS = 16_000_000L
        private const val GLIDE_SETTLES = 2
        private const val HEIGHT = 400
        private const val SETTLE_FRAMES = 10
        private const val WIDTH = 900
        private val AREPAS_CLOSE = Offset(x = 91f, y = 17f)
        private val AREPAS_TAB = Offset(x = 40f, y = 17f)
        private val BEFORE_IDEAS = Offset(x = 230f, y = 17f)
        private val IDEAS_TAB = Offset(x = 268f, y = 17f)
        private val LEFT_PANE = Offset(x = 200f, y = 200f)
        private val RIGHT_EDGE = Offset(x = 820f, y = 200f)
        private val RIGHT_PANE = Offset(x = 650f, y = 200f)
        private val SPLIT_RIGHT = Offset(x = 85f, y = 145f)
        private val WELCOME_TAB = Offset(x = 160f, y = 17f)
    }
}
