package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.domain.TableAction
import app.notekea.editor.domain.TableEditing
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A table edited by cells: the text field is over one cell, the keys move between cells, the bar edits the table. */
@OptIn(ExperimentalTestApi::class)
class EditorTableCellsTest {
    private val buffer = TextFieldState(
        initialText = BODY,
        initialSelection = TextRange(index = 0)
    )

    private val actions = mutableListOf<TableAction>()

    @Test
    fun `a click on a drawn table's cell puts the cursor in it and the field shows the cell alone`() =
        runComposeUiTest {
            show()

            clickCell(text = "Salt")

            assertEquals(
                expected = "Salt",
                actual = fieldText()
            )

            assertEquals(
                expected = "Salt",
                actual = cursorCell()
            )

            assertTrue(actual = bar(label = "Insert row below").fetchSemanticsNodes().isNotEmpty())
        }

    @Test
    fun `the bar is not shown while the cursor is out of the table`() = runComposeUiTest {
        show()

        assertTrue(actual = bar(label = "Insert row below").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `a pipe typed in a cell is escaped and the table stays whole`() = runComposeUiTest {
        show()
        clickCell(text = "Salt")

        field().performTextInput(text = "a|b")
        waitForIdle()

        assertTrue(actual = buffer.text.contains(other = "a\\|b"))

        assertEquals(
            expected = BODY.lines().size,
            actual = buffer
                .text
                .lines()
                .size
        )
    }

    @Test
    fun `Tab goes to the next cell and, after the last one, asks for a new row`() =
        runComposeUiTest {
            show()
            clickCell(text = "Salt")

            field().performKeyInput { pressKey(key = Key.Tab) }
            waitForIdle()

            assertEquals(
                expected = "1",
                actual = cursorCell()
            )

            field().performKeyInput { pressKey(key = Key.Tab) }
            waitForIdle()

            assertEquals(
                expected = listOf(TableAction.NEXT_ROW),
                actual = actions
            )

            assertTrue(actual = buffer.text.contains(other = "| Salt | 1 |\n|  |  |\n"))
        }

    @Test
    fun `Enter goes to the cell below and the arrows leave the table at its edges`() =
        runComposeUiTest {
            show()
            clickCell(text = "Corn flour")

            field().performKeyInput { pressKey(key = Key.Enter) }
            waitForIdle()

            assertEquals(
                expected = "Salt",
                actual = cursorCell()
            )

            field().performKeyInput { pressKey(key = Key.DirectionDown) }
            waitForIdle()

            assertEquals(
                expected = BODY.indexOf(string = "After"),
                actual = buffer.selection.start
            )
        }

    @Test
    fun `Backspace at a cell's start deletes nothing`() = runComposeUiTest {
        show()
        clickCell(text = "Salt")

        buffer.edit { selection = TextRange(index = BODY.indexOf(string = "Salt")) }
        waitForIdle()

        field().performKeyInput { pressKey(key = Key.Backspace) }
        waitForIdle()

        assertEquals(
            expected = BODY,
            actual = buffer.text.toString()
        )
    }

    @Test
    fun `a button of the bar edits the table from the cursor's cell`() = runComposeUiTest {
        show()
        clickCell(text = "Salt")

        bar(label = "Delete row")[0].performClick()
        waitForIdle()

        assertEquals(
            expected = listOf(TableAction.DELETE_ROW),
            actual = actions
        )

        assertTrue(actual = !buffer.text.contains(other = "Salt"))
    }

    @Test
    fun `a click over a drawn table, where its bar goes, puts the cursor in the table`() =
        runComposeUiTest {
            show()

            onNodeWithText(text = "Item ").performMouseInput {
                click(
                    position = Offset(
                        x = 0f,
                        y = BAND_ABOVE_HEADER_TEXT
                    )
                )
            }

            waitForIdle()

            assertEquals(
                expected = "Item",
                actual = cursorCell()
            )
        }

    @Test
    fun `a click on another cell still lands in it after a paragraph is written above the table`() =
        runComposeUiTest {
            show()
            clickCell(text = "Salt")

            val added = "A new paragraph.\n\n"
            val cursor = buffer.selection.start

            buffer.edit {
                replace(
                    start = 0,
                    end = 0,
                    text = added
                )

                selection = TextRange(index = cursor + added.length)
            }

            waitForIdle()
            clickCell(text = "Corn flour")

            assertEquals(
                expected = "Corn flour",
                actual = cursorCell()
            )
        }

    private fun ComposeUiTest.show() {
        val editing = TableEditing()

        setContent {
            NotekeaTheme(darkTheme = false) {
                EditorContent(
                    state = EditorState(notePath = RelativePath(value = NOTE)),
                    buffer = buffer,
                    platformConventions = PreviewPlatformConventions.linux,
                    onIntent = { intent ->
                        if (intent is EditorIntent.OnTableEdit) {
                            actions += intent.action

                            write(
                                editing = editing,
                                action = intent.action
                            )
                        }
                    }
                )
            }
        }
    }

    /** What the editor does with a table's action: the edit written into the note. */
    private fun write(
        editing: TableEditing,
        action: TableAction
    ) {
        val edit = editing.edit(
            text = buffer.text.toString(),
            cursor = buffer.selection.end,
            action = action
        ) ?: return

        buffer.edit {
            replace(
                start = edit.start,
                end = edit.end,
                text = edit.replacement
            )

            selection = TextRange(
                start = edit.selectionStart,
                end = edit.selectionEnd
            )
        }
    }

    private fun ComposeUiTest.clickCell(text: String) {
        // The renderer draws a cell's text with the space that ends it.
        onNodeWithText(text = "$text ").performMouseInput { click() }
        waitForIdle()
    }

    private fun ComposeUiTest.bar(label: String) =
        onAllNodes(matcher = hasContentDescription(value = label))

    /** The body's field: the note's title is the first text field of the editor. */
    private fun ComposeUiTest.field() = onAllNodes(matcher = hasSetTextAction())[1]

    /** The text of the cell the cursor is in: what lies between the pipes around it. */
    private fun cursorCell(): String {
        val text = buffer.text.toString()
        val cursor = buffer.selection.start

        return text
            .substring(
                startIndex = text.lastIndexOf(
                    char = '|',
                    startIndex = cursor - 1
                ) + 1,
                endIndex = text.indexOf(
                    char = '|',
                    startIndex = cursor
                )
            )
            .trim()
    }

    /** The text the field lays out: its semantics' text is the note's, not what is shown of it. */
    private fun ComposeUiTest.fieldText(): String? {
        val layouts = mutableListOf<TextLayoutResult>()

        field()
            .fetchSemanticsNode()
            .config
            .getOrNull(key = SemanticsActions.GetTextLayoutResult)
            ?.action
            ?.invoke(layouts)

        return layouts
            .firstOrNull()
            ?.layoutInput
            ?.text
            ?.text
    }

    companion object {
        private const val BAND_ABOVE_HEADER_TEXT = -30f
        private const val BODY =
            "# Arepas\n\n| Item | Qty |\n| :-- | --: |\n| Corn flour | 2 |\n| Salt | 1 |\n\nAfter the table.\n"
        private const val NOTE = "Recipes/Arepas.md"
    }
}
