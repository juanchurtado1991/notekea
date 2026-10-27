package app.notekea.notes.presentation.tree

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.NoteStats
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.EditorState
import app.notekea.notes.presentation.editor.NoteInfo
import app.notekea.notes.presentation.workspace.TabKind
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The workspace's status bar (`ROADMAP.md` 1.14), rendered headless: the vault and the note's folders, the scan while it
 * runs, then the cursor's place in the file (after the frontmatter's lines), the line endings, the encoding and the
 * words; Preview leaves the cursor out.
 */
class WorkspaceStatusBarScreenTest {
    @Test
    fun `the bar says where the note is, the scan, the cursor in the file, its format, words and save state`() {
        assertEquals(
            expected = listOf(
                "Playground Vault",
                "Editor",
                "Tasks and callouts",
                "Loading notes…",
                "6:6",
                "CRLF",
                "UTF-8",
                "236 words",
                "Saved"
            ) to listOf("Playground Vault", "Editor", "Tasks and callouts", "LF", "UTF-8", "Saved"),
            actual = texts(isReading = false) to texts(isReading = true)
        )
    }

    @OptIn(InternalComposeUiApi::class)
    private fun texts(
        isReading: Boolean
    ): List<String> {
        var texts = emptyList<String>()
        val editor = EditorState(
            isReading = isReading,
            usesCrLf = !isReading,
            bodyStartLine = 3,
            info = NoteInfo(
                stats = NoteStats(words = 236, links = 0, tags = 2, tasks = 9, doneTasks = 4).takeUnless {
                    isReading
                }
            )
        )
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    WorkspaceStatusBar(
                        vaultName = VAULT,
                        isLoading = !isReading,
                        onReveal = {},
                        path = RelativePath(value = NOTE),
                        kind = TabKind.NOTE,
                        editor = editor,
                        buffer = TextFieldState(initialText = BODY, initialSelection = TextRange(index = CURSOR))
                    )
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                texts = scene.semanticsOwners.flatMap { it.getAllSemanticsNodes(mergingEnabled = false) }
                    .mapNotNull { node -> node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text } }
            }
        }
        return texts
    }

    companion object {
        private const val BODY = "# Tasks\n\nLine two\nAnd three"
        private const val CURSOR = 14
        private const val HEIGHT = 40
        private const val NOTE = "Editor/Tasks and callouts.md"
        private const val VAULT = "Playground Vault"
        private const val WIDTH = 1300
    }
}
