package app.notekea.tree.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.tree.presentation.model.TreePreviewData
import app.notekea.tree.presentation.model.VaultTreeIntent
import kotlin.test.Test
import kotlin.test.assertEquals

/** A note dragged onto a folder in the tree (the developer's request of 2026-11-14). */
@OptIn(ExperimentalTestApi::class)
class TreeDragTest {
    @Test
    fun `a note dragged onto a folder is moved into it, and a plain click still only clicks`() =
        runComposeUiTest {
            val intents = mutableListOf<VaultTreeIntent>()

            setContent {
                NotekeaTheme(darkTheme = false) {
                    Box(
                        modifier = Modifier.size(
                            width = 300.dp,
                            height = 500.dp
                        )
                    ) {
                        TreeList(
                            state = TreePreviewData.state(),
                            onIntent = { intents += it }
                        )
                    }
                }
            }

            waitForIdle()

            val from = onNodeWithText(text = "Welcome")
                .fetchSemanticsNode()
                .boundsInRoot
                .center

            val to = onNodeWithText(text = "Recipes")
                .fetchSemanticsNode()
                .boundsInRoot
                .center

            onNodeWithText(text = "Welcome").performMouseInput {
                val start = center
                moveTo(position = start)
                press()

                (1..STEPS).forEach { step ->
                    moveTo(
                        position = start + Offset(
                            x = 0f,
                            y = (to.y - from.y) * step / STEPS
                        )
                    )
                }

                release()
            }

            waitForIdle()

            assertEquals(
                expected = listOf(
                    VaultTreeIntent.OnMoveInto(
                        path = RelativePath(value = "Welcome.md"),
                        folder = RelativePath(value = "Recipes")
                    )
                ),
                actual = intents.filterIsInstance<VaultTreeIntent.OnMoveInto>()
            )
        }

    // Code review, 2026-11-14: a mouse moves a pixel or two during a click, and that was taken for a drag, which
    // swallowed the click.
    @Test
    fun `a click during which the mouse moves a little still opens the note, and moves nothing`() =
        runComposeUiTest {
            val intents = mutableListOf<VaultTreeIntent>()

            setContent {
                NotekeaTheme(darkTheme = false) {
                    Box(
                        modifier = Modifier.size(
                            width = 300.dp,
                            height = 500.dp
                        )
                    ) {
                        TreeList(
                            state = TreePreviewData.state(),
                            onIntent = { intents += it }
                        )
                    }
                }
            }

            waitForIdle()

            onNodeWithText(text = "Welcome").performMouseInput {
                moveTo(position = center)
                press()

                moveTo(
                    position = center + Offset(
                        x = 1f,
                        y = 2f
                    )
                )

                release()
            }

            waitForIdle()

            assertEquals(
                expected = listOf(1, 0),
                actual = listOf(
                    intents.filterIsInstance<VaultTreeIntent.OnRowClick>().size,
                    intents.filterIsInstance<VaultTreeIntent.OnMoveInto>().size
                )
            )
        }

    @Test
    fun `a right click on a row still opens its menu`() = runComposeUiTest {
        setContent {
            NotekeaTheme(darkTheme = false) {
                Box(
                    modifier = Modifier.size(
                        width = 300.dp,
                        height = 500.dp
                    )
                ) {
                    TreeList(
                        state = TreePreviewData.state(),
                        onIntent = {}
                    )
                }
            }
        }

        waitForIdle()
        onNodeWithText(text = "Recipes").performMouseInput { rightClick() }
        waitForIdle()
        onNodeWithText(text = "New note from template").assertExists()
    }

    private companion object {
        const val STEPS = 8
    }
}
