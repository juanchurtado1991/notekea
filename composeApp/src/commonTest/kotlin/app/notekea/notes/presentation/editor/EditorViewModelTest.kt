package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.ListAction
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeNoteFileOperations
import app.notekea.notes.domain.SaveEditorModeUseCase
import app.notekea.notes.domain.GetEditorModeUseCase
import app.notekea.notes.domain.FakeEditorPreferencesStore
import app.notekea.notes.domain.FakeNoteScanner
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.LinkUpdateResult
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.ObserveNoteOnDiskUseCase
import app.notekea.notes.domain.OpenNoteUseCase
import app.notekea.notes.domain.PlanLinkUpdatesUseCase
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.RenameAndUpdateLinksUseCase
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.UpdateLinksAfterRenameUseCase
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SaveNoteUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The editor over real use cases and fake ports. The open note is polled forever, so time only moves with
 * [advanceTimeBy] and [runCurrent], never `advanceUntilIdle`, and every test ends by cancelling the ViewModel's scope.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val scanner = FakeNoteScanner()
    private val store = FakeNoteContentStore()
    private val operations = FakeNoteFileOperations()
    private val preferences = FakeEditorPreferencesStore()
    private val viewModel by lazy {
        EditorViewModel(
            vaultPath = VAULT,
            openNote = OpenNoteUseCase(noteContentStore = store),
            saveNote = SaveNoteUseCase(noteContentStore = store),
            observeNoteOnDisk = ObserveNoteOnDiskUseCase(noteScanner = scanner, noteContentStore = store),
            renameAndUpdateLinks = RenameAndUpdateLinksUseCase(
                noteScanner = scanner,
                renameEntry = RenameEntryUseCase(
                    noteFileOperations = operations,
                    validateFileName = ValidateFileNameUseCase()
                ),
                updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
                    planLinkUpdates = PlanLinkUpdatesUseCase(
                        noteContentStore = store,
                        linkFinder = JetBrainsMarkdownLinkFinder()
                    ),
                    applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)
                )
            ),
            getEditorMode = GetEditorModeUseCase(editorPreferencesStore = preferences),
            saveEditorMode = SaveEditorModeUseCase(editorPreferencesStore = preferences)
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        // Like the real store, which switches to the IO dispatcher: a save that cancels itself is stopped there.
        store.suspendsLikeDisk = true
        putOnDisk(path = AREPAS, text = AREPAS_TEXT)
        putOnDisk(path = WELCOME, text = "# Welcome\n")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `an opened note shows its body, and edits are saved a second after the last keystroke`() = runEditorTest {
        open(path = AREPAS)
        val opened = viewModel.buffer.text.toString()

        type(text = "# Arepas reina\n")
        advanceTimeBy(delayTimeMillis = HALF_SECOND_MILLIS)
        val beforeTheDelay = store.texts.getValue(AREPAS)
        advanceTimeBy(delayTimeMillis = SECOND_MILLIS)
        runCurrent()

        assertEquals(
            expected = listOf("# Arepas\n", AREPAS_TEXT, "---\ntags: [recipe]\n---\n# Arepas reina\n"),
            actual = listOf(opened, beforeTheDelay, store.texts.getValue(AREPAS))
        )
        assertEquals(
            expected = SaveStatus.SAVED,
            actual = viewModel.state.value.saveStatus
        )
    }

    @Test
    fun `typing shows unsaved, and undoing every edit leaves nothing to save`() = runEditorTest {
        open(path = AREPAS)

        type(text = "# Changed\n")
        val unsaved = viewModel.state.value.saveStatus
        type(text = "# Arepas\n")
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        runCurrent()

        assertEquals(
            expected = listOf(SaveStatus.UNSAVED, SaveStatus.NONE),
            actual = listOf(unsaved, viewModel.state.value.saveStatus)
        )
        assertEquals(
            expected = AREPAS_TEXT,
            actual = store.texts.getValue(AREPAS)
        )
    }

    @Test
    fun `save, opening another note and closing the window all save at once`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        type(text = "Saved by the shortcut")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()
        val bySave = store.texts.getValue(AREPAS)
        type(text = "Saved before switching")
        open(path = WELCOME)
        val bySwitch = store.texts.getValue(AREPAS)
        type(text = "Saved before closing")
        viewModel.onIntent(intent = EditorIntent.OnFlushRequest)
        runCurrent()

        assertEquals(
            expected = listOf(
                "---\ntags: [recipe]\n---\nSaved by the shortcut",
                "---\ntags: [recipe]\n---\nSaved before switching",
                "Saved before closing"
            ),
            actual = listOf(bySave, bySwitch, store.texts.getValue(WELCOME))
        )
        assertEquals(
            expected = listOf<EditorEffect>(EditorEffect.Flushed),
            actual = effects
        )
    }

    @Test
    fun `the editor's own save is not taken for an outside change`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        scanner.addNote(file = file(path = AREPAS.value, modifiedOffsetMillis = 1L))
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)

        assertEquals(
            expected = false to "Mine",
            actual = viewModel.state.value.hasConflict to viewModel.buffer.text.toString()
        )
    }

    @Test
    fun `an outside change reloads a note without unsaved edits`() = runEditorTest {
        open(path = AREPAS)

        changeOutside(text = "---\ntags: [recipe]\n---\n# Arepas from the phone\n")

        assertEquals(
            expected = false to "# Arepas from the phone\n",
            actual = viewModel.state.value.hasConflict to viewModel.buffer.text.toString()
        )
    }

    @Test
    fun `an outside change with unsaved edits asks, and autosave waits for the answer`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")

        changeOutside(text = "Theirs")
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        runCurrent()

        assertEquals(
            expected = Triple(true, "Theirs", "Mine"),
            actual = Triple(
                viewModel.state.value.hasConflict,
                store.texts.getValue(AREPAS),
                viewModel.buffer.text.toString()
            )
        )
    }

    @Test
    fun `keep mine overwrites the outside change with the editor's whole note`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")
        changeOutside(text = "Theirs")

        viewModel.onIntent(intent = EditorIntent.OnKeepMine)
        runCurrent()

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nMine" to false,
            actual = store.texts.getValue(AREPAS) to viewModel.state.value.hasConflict
        )
    }

    @Test
    fun `load theirs drops the unsaved edits`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Mine")
        changeOutside(text = "Theirs")

        viewModel.onIntent(intent = EditorIntent.OnLoadTheirs)
        runCurrent()

        assertEquals(
            expected = Triple("Theirs", false, SaveStatus.NONE),
            actual = Triple(
                viewModel.buffer.text.toString(),
                viewModel.state.value.hasConflict,
                viewModel.state.value.saveStatus
            )
        )
    }

    @Test
    fun `a note deleted while open can be restored with the editor's text`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Still here")

        deleteOutside()
        val deleted = viewModel.state.value.isDeleted
        viewModel.onIntent(intent = EditorIntent.OnRestore)
        runCurrent()

        assertEquals(
            expected = Triple(true, false, "---\ntags: [recipe]\n---\nStill here"),
            actual = Triple(deleted, viewModel.state.value.isDeleted, store.texts.getValue(AREPAS))
        )
    }

    @Test
    fun `closing a deleted note empties the editor and says so`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)
        deleteOutside()

        viewModel.onIntent(intent = EditorIntent.OnCloseDeleted)
        runCurrent()

        assertEquals(
            expected = Triple(null, "", listOf<EditorEffect>(EditorEffect.Closed)),
            actual = Triple(viewModel.state.value.notePath, viewModel.buffer.text.toString(), effects)
        )
    }

    @Test
    fun `after a move in Notekea, edits are saved at the new path only`() = runEditorTest {
        open(path = AREPAS)
        val moved = RelativePath(value = "Recipes/Arepas reina.md")
        store.texts[moved] = store.texts.remove(AREPAS).orEmpty()
        scanner.files.remove(AREPAS)
        scanner.addNote(file = file(path = moved.value))

        viewModel.onIntent(intent = EditorIntent.OnNoteMoved(path = moved))
        type(text = "Edited after the move")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nEdited after the move" to null,
            actual = store.texts[moved] to store.texts[AREPAS]
        )
    }

    @Test
    fun `the title renames the note after saving it, and the notes that link to it follow`() = runEditorTest {
        val effects = collectEffects()
        store.texts[WELCOME] = "Cook [[Arepas]]."
        open(path = AREPAS)
        type(text = "Saved before the rename")

        viewModel.onIntent(intent = EditorIntent.OnTitleCommit(name = "Arepas reina"))
        runCurrent()

        val renamed = RelativePath(value = "Recipes/Arepas reina.md")
        assertEquals(
            expected = listOf(
                "---\ntags: [recipe]\n---\nSaved before the rename",
                "Cook [[Arepas reina]]."
            ),
            actual = listOf(store.texts.getValue(AREPAS), store.texts.getValue(WELCOME))
        )
        assertEquals<Triple<Any?, Any?, Any?>>(
            expected = Triple(
                renamed,
                listOf(AREPAS to renamed),
                listOf<EditorEffect>(
                    EditorEffect.Renamed(
                        from = AREPAS,
                        to = renamed,
                        links = LinkUpdateResult(updated = listOf(WELCOME), skipped = emptyList())
                    )
                )
            ),
            actual = Triple(viewModel.state.value.notePath, operations.moves, effects)
        )
    }

    @Test
    fun `a refused title is explained until it is edited, and the same title changes nothing`() = runEditorTest {
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnTitleCommit(name = " Arepas "))
        viewModel.onIntent(intent = EditorIntent.OnTitleCommit(name = "a/b"))
        runCurrent()
        val refused = viewModel.state.value.titleError
        viewModel.onIntent(intent = EditorIntent.OnTitleErrorDismiss)

        assertEquals<Triple<Any?, Any?, Any?>>(
            expected = Triple(NotesError.InvalidName, null, emptyList<Pair<RelativePath, RelativePath>>()),
            actual = Triple(refused, viewModel.state.value.titleError, operations.moves)
        )
    }

    @Test
    fun `leaving the text saves at once`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Saved on focus loss")

        viewModel.onIntent(intent = EditorIntent.OnFocusLost)
        runCurrent()

        assertEquals(
            expected = "---\ntags: [recipe]\n---\nSaved on focus loss",
            actual = store.texts.getValue(AREPAS)
        )
    }

    @Test
    fun `a save never brings back a note that is gone, it offers to restore it`() = runEditorTest {
        open(path = AREPAS)
        type(text = "Edited")
        store.texts.remove(AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        assertEquals(
            expected = null to true,
            actual = store.texts[AREPAS] to viewModel.state.value.isDeleted
        )
    }

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
    fun `a focus request for the open note gives its text the keyboard at once`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFocusRequest())
        runCurrent()

        assertEquals(
            expected = listOf<EditorEffect>(EditorEffect.FocusText),
            actual = effects
        )
    }

    @Test
    fun `a focus request for a note chosen in the quick switcher waits until it has opened`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFocusRequest(path = WELCOME))
        runCurrent()
        val beforeOpening = effects.toList()
        open(path = WELCOME)

        assertEquals(
            expected = emptyList<EditorEffect>() to listOf<EditorEffect>(EditorEffect.FocusText),
            actual = beforeOpening to effects
        )
    }

    @Test
    fun `a focus request is dropped when another note opens instead`() = runEditorTest {
        val effects = collectEffects()
        open(path = AREPAS)

        viewModel.onIntent(intent = EditorIntent.OnFocusRequest(path = WELCOME))
        runCurrent()
        open(path = RelativePath(value = "Other.md"))
        open(path = WELCOME)

        assertEquals(
            expected = emptyList<EditorEffect>(),
            actual = effects
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

    @Test
    fun `a note that cannot be read shows the error until a retry opens it`() = runEditorTest {
        val missing = RelativePath(value = "Missing.md")
        open(path = missing)
        val failed = viewModel.state.value.loadError

        store.texts[missing] = "Found"
        viewModel.onIntent(intent = EditorIntent.OnRetryOpen)
        runCurrent()

        assertEquals(
            expected = Triple(NotesError.OperationFailed(path = missing.value), null, "Found"),
            actual = Triple(failed, viewModel.state.value.loadError, viewModel.buffer.text.toString())
        )
    }

    @Test
    fun `a refused save is shown and the next save succeeds`() = runEditorTest {
        open(path = AREPAS)
        store.refused += AREPAS

        type(text = "Locked")
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()
        val error = viewModel.state.value.saveError
        store.refused -= AREPAS
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
        runCurrent()

        assertEquals(
            expected = Triple(NotesError.OperationFailed(path = AREPAS.value), null, SaveStatus.SAVED),
            actual = Triple(error, viewModel.state.value.saveError, viewModel.state.value.saveStatus)
        )
    }

    /** Runs [body], then stops the ViewModel: its disk polling never ends, and `runTest` would wait for it forever. */
    private fun runEditorTest(
        body: suspend TestScope.() -> Unit
    ) = runTest {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    private fun TestScope.open(
        path: RelativePath
    ) {
        viewModel.onIntent(intent = EditorIntent.OnOpen(path = path))
        runCurrent()
    }

    /** Types into the text field: replaces the body and lets the ViewModel see the change. */
    private fun TestScope.type(
        text: String
    ) {
        viewModel.buffer.edit { replace(start = 0, end = length, text = text) }
        Snapshot.sendApplyNotifications()
        runCurrent()
    }

    private fun TestScope.changeOutside(
        text: String
    ) {
        store.texts[AREPAS] = text
        scanner.addNote(file = file(path = AREPAS.value, modifiedOffsetMillis = 1L))
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        Snapshot.sendApplyNotifications()
        runCurrent()
    }

    private fun TestScope.deleteOutside() {
        store.texts.remove(AREPAS)
        scanner.files.remove(AREPAS)
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
    }

    private fun TestScope.collectEffects(): List<EditorEffect> {
        val effects = mutableListOf<EditorEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        return effects
    }

    private fun putOnDisk(
        path: RelativePath,
        text: String
    ) {
        store.texts[path] = text
        scanner.addNote(file = file(path = path.value))
        operations.existing += path
    }

    companion object {
        private const val TASKS_TEXT = "- [ ] Buy flour\n- [x] Soak beans\n"
        private const val AREPAS_TEXT = "---\ntags: [recipe]\n---\n# Arepas\n"
        private const val HALF_SECOND_MILLIS = 500L
        private const val SECOND_MILLIS = 1_000L
        private const val TWO_SECONDS_MILLIS = 2_000L
        private val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        private val TASKS = RelativePath(value = "Tasks.md")
        private val WELCOME = RelativePath(value = "Welcome.md")
    }
}
