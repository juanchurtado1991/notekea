package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.ListAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

/** Find, modes, tasks, toolbar formats, pickers, list keys and links clicked in Read mode. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorTextEditsTest : EditorViewModelTestBase() {
    @Test
    fun `find counts the matches and moves through them in both directions, wrapping around`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Arepas, arepas and more AREPAS")

        viewModel.onIntent(intent = EditorIntent.OnFindOpen)
        viewModel.onIntent(intent = EditorIntent.OnFindQueryChange(query = "arepas"))
        val first = viewModel.state.value.find
        viewModel.onIntent(intent = EditorIntent.OnFindPrevious)
        val wrapped = viewModel.state.value.find?.current
        viewModel.onIntent(intent = EditorIntent.OnFindNext)

        assertEquals(
            expected = FindState(query = "arepas", matches = listOf(0..5, 8..13, 24..29), current = 0),
            actual = first
        )
        assertEquals(
            expected = 2 to 0,
            actual = wrapped to viewModel.state.value.find?.current
        )
    }

    @Test
    fun `closing find leaves the current match selected, and edits keep the matches up to date`() = runEditorTest {
        open(path = AREPAS)
        type(text = "one two one")
        viewModel.onIntent(intent = EditorIntent.OnFindOpen)
        viewModel.onIntent(intent = EditorIntent.OnFindQueryChange(query = "one"))
        viewModel.onIntent(intent = EditorIntent.OnFindNext)

        type(text = "one two one one")
        val afterEdit = viewModel.state.value.find
        viewModel.onIntent(intent = EditorIntent.OnFindClose)

        assertEquals(
            expected = FindState(query = "one", matches = listOf(0..2, 8..10, 12..14), current = 1),
            actual = afterEdit
        )
        assertEquals(
            expected = null to TextRange(start = 8, end = 11),
            actual = viewModel.state.value.find to viewModel.buffer.selection
        )
    }

    @Test
    fun `a query that is not in the note has no current match`() = runEditorTest {
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFindOpen)
        viewModel.onIntent(intent = EditorIntent.OnFindQueryChange(query = "pizza"))
        viewModel.onIntent(intent = EditorIntent.OnFindNext)

        assertEquals(
            expected = FindState(query = "pizza", matches = emptyList(), current = null),
            actual = viewModel.state.value.find
        )
    }

    @Test
    fun `the chosen mode closes find, is kept for the next note and is remembered for the next run`() = runEditorTest {
        open(path = AREPAS)
        viewModel.onIntent(intent = EditorIntent.OnFindOpen)

        viewModel.onIntent(intent = EditorIntent.OnReadModeToggle)
        runCurrent()
        val reading = viewModel.state.value
        open(path = WELCOME)

        assertEquals(
            expected = listOf(true, null, true, true),
            actual = listOf(reading.isReading, reading.find, viewModel.state.value.isReading, preferences.isReading)
        )
    }

    @Test
    fun `notes open in the mode remembered from an earlier run`() = runEditorTest {
        preferences.isReading = true

        open(path = AREPAS)

        assertEquals(
            expected = true,
            actual = viewModel.state.value.isReading
        )
    }

    @Test
    fun `clicking a task checks it in the text, which is saved like any edit`() = runEditorTest {
        putOnDisk(path = TASKS, text = TASKS_TEXT)
        open(path = TASKS)
        val flour = viewModel.buffer.text.indexOf(string = "[ ] Buy flour") + 1

        viewModel.onIntent(intent = EditorIntent.OnTaskToggle(stateIndex = flour))
        Snapshot.sendApplyNotifications()
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        runCurrent()

        assertEquals(
            expected = "- [x] Buy flour\n- [x] Soak beans\n",
            actual = store.texts.getValue(TASKS)
        )
    }

    @Test
    fun `mod+enter toggles the task on the cursor line in edit mode only`() = runEditorTest {
        putOnDisk(path = TASKS, text = TASKS_TEXT)
        open(path = TASKS)
        viewModel.buffer.edit { selection = TextRange(index = length - 2) }

        viewModel.onIntent(intent = EditorIntent.OnTaskToggleShortcut)
        val inEditMode = viewModel.buffer.text.toString()
        viewModel.onIntent(intent = EditorIntent.OnReadModeToggle)
        runCurrent()
        viewModel.onIntent(intent = EditorIntent.OnTaskToggleShortcut)

        assertEquals(
            expected = listOf("- [ ] Buy flour\n- [ ] Soak beans\n", "- [ ] Buy flour\n- [ ] Soak beans\n"),
            actual = listOf(inEditMode, viewModel.buffer.text.toString())
        )
    }

    @Test
    fun `a click on what is no longer a task changes nothing`() = runEditorTest {
        putOnDisk(path = TASKS, text = TASKS_TEXT)
        open(path = TASKS)

        viewModel.onIntent(intent = EditorIntent.OnTaskToggle(stateIndex = 0))
        viewModel.onIntent(intent = EditorIntent.OnTaskToggle(stateIndex = TASKS_TEXT.length + 5))

        assertEquals(
            expected = TASKS_TEXT,
            actual = viewModel.buffer.text.toString()
        )
    }

    @Test
    fun `a toolbar action formats the selection as one edit, saved like typing, and gives the text the keyboard`() =
        runEditorTest {
            val effects = collectEffects()
            putOnDisk(path = TASKS, text = TASKS_TEXT)
            open(path = TASKS)
            val flour = TASKS_TEXT.indexOf(string = "flour")
            viewModel.buffer.edit { selection = TextRange(start = flour, end = flour + "flour".length) }

            viewModel.onIntent(intent = EditorIntent.OnFormat(action = FormatAction.Bold(placeholder = "bold text")))
            runCurrent()
            val selected = viewModel.buffer.selection
            Snapshot.sendApplyNotifications()
            advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
            runCurrent()

            assertEquals(
                expected = Triple(
                    "- [ ] Buy **flour**\n- [x] Soak beans\n",
                    TextRange(start = flour + 2, end = flour + 2 + "flour".length),
                    listOf<EditorEffect>(EditorEffect.FocusText)
                ),
                actual = Triple(store.texts.getValue(TASKS), selected, effects.toList())
            )
        }

    @Test
    fun `formatting and asking for a note to link work in edit mode only`() = runEditorTest {
        val effects = collectEffects()
        putOnDisk(path = TASKS, text = TASKS_TEXT)
        open(path = TASKS)

        viewModel.onIntent(intent = EditorIntent.OnNoteLinkRequest)
        runCurrent()
        viewModel.onIntent(intent = EditorIntent.OnReadModeToggle)
        runCurrent()
        viewModel.onIntent(intent = EditorIntent.OnNoteLinkRequest)
        viewModel.onIntent(intent = EditorIntent.OnFormat(action = FormatAction.Divider))
        runCurrent()

        assertEquals(
            expected = TASKS_TEXT to listOf<EditorEffect>(EditorEffect.RequestNoteLink),
            actual = viewModel.buffer.text.toString() to effects.toList()
        )
    }

    @Test
    fun `the selection is kept while the quick switcher picks a note, which links it, or comes back on closing`() =
        runEditorTest {
            putOnDisk(path = TASKS, text = TASKS_TEXT)
            open(path = TASKS)
            val flour = TASKS_TEXT.indexOf(string = "flour")
            val select = {
                viewModel.buffer.edit { selection = TextRange(start = flour, end = flour + "flour".length) }
            }
            // The palette takes the keyboard, and the text field drops its selection.
            val pickerTakesFocus = { viewModel.buffer.edit { selection = TextRange(index = 0) } }

            select()
            viewModel.onIntent(intent = EditorIntent.OnNoteLinkRequest)
            pickerTakesFocus()
            viewModel.onIntent(intent = EditorIntent.OnFocusRequest())
            val restored = viewModel.buffer.selection
            viewModel.onIntent(intent = EditorIntent.OnNoteLinkRequest)
            pickerTakesFocus()
            viewModel.onIntent(intent = EditorIntent.OnFormat(action = FormatAction.NoteLink(target = "Flour")))

            assertEquals(
                expected = TextRange(start = flour, end = flour + "flour".length) to
                    "- [ ] Buy [[Flour|flour]]\n- [x] Soak beans\n",
                actual = restored to viewModel.buffer.text.toString()
            )
        }

    @Test
    fun `tag opens the tag picker with the selected text typed, and the chosen tag takes the selection's place`() =
        runEditorTest {
            val effects = collectEffects()
            putOnDisk(path = TASKS, text = TASKS_TEXT)
            open(path = TASKS)
            val flour = TASKS_TEXT.indexOf(string = "flour")
            viewModel.buffer.edit { selection = TextRange(start = flour, end = flour + "flour".length) }

            viewModel.onIntent(intent = EditorIntent.OnTagRequest)
            runCurrent()
            viewModel.buffer.edit { selection = TextRange(index = 0) }
            viewModel.onIntent(intent = EditorIntent.OnFormat(action = FormatAction.Tag(name = "flour")))
            runCurrent()

            assertEquals(
                expected = EditorEffect.RequestTag(query = "flour") to "- [ ] Buy #flour \n- [x] Soak beans\n",
                actual = effects.first() to viewModel.buffer.text.toString()
            )
        }

    @Test
    fun `list keys edit the list at the cursor in edit mode`() = runEditorTest {
        putOnDisk(path = TASKS, text = TASKS_TEXT)
        open(path = TASKS)
        viewModel.buffer.edit { selection = TextRange(index = TASKS_TEXT.indexOf(char = '\n')) }

        viewModel.onIntent(intent = EditorIntent.OnListKey(action = ListAction.NEW_ITEM))

        assertEquals(
            expected = "- [ ] Buy flour\n- [ ] \n- [x] Soak beans\n",
            actual = viewModel.buffer.text.toString()
        )
    }

    @Test
    fun `a tag clicked in read mode asks to search it`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnTagClick(tag = "recipe/venezuelan"))
        runCurrent()

        assertEquals(
            expected = listOf<EditorEffect>(EditorEffect.SearchTag(tag = "recipe/venezuelan")),
            actual = effects
        )
    }

    @Test
    fun `a link clicked in read mode asks to open its note from the open one`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(
            intent = EditorIntent.OnLinkClick(link = ReadLink.Note(target = "Welcome", kind = LinkKind.WIKI))
        )
        runCurrent()

        assertEquals(
            expected = listOf<EditorEffect>(
                EditorEffect.OpenLink(source = AREPAS, target = "Welcome", kind = LinkKind.WIKI)
            ),
            actual = effects
        )
    }

    @Test
    fun `find switches back to edit mode, which is then remembered`() = runEditorTest {
        open(path = AREPAS)
        viewModel.onIntent(intent = EditorIntent.OnReadModeToggle)

        viewModel.onIntent(intent = EditorIntent.OnFindOpen)
        runCurrent()

        assertEquals(
            expected = Triple(false, FindState(), false),
            actual = Triple(viewModel.state.value.isReading, viewModel.state.value.find, preferences.isReading)
        )
    }
}
