package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.viewModelScope
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.RelativePath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/**
 * The editor over real use cases and fake ports. The open note is polled forever, so time only moves with
 * [advanceTimeBy] and [runCurrent], never `advanceUntilIdle`, and every test ends by cancelling the ViewModel's scope.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class EditorViewModelTestBase {
    protected val testDispatcher = StandardTestDispatcher()
    protected val fixture = EditorViewModels(dispatcher = testDispatcher)
    protected val scanner = fixture.scanner
    protected val store = fixture.store
    protected val operations = fixture.operations
    protected val preferences = fixture.preferences
    protected val viewModel by lazy { fixture.create() }

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

    /** Runs [body], then stops the ViewModel: its disk polling never ends, and `runTest` would wait for it forever. */
    protected fun runEditorTest(
        body: suspend TestScope.() -> Unit
    ) = runTest {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    protected fun TestScope.open(
        path: RelativePath
    ) {
        viewModel.onIntent(intent = EditorIntent.OnOpen(path = path))
        runCurrent()
    }

    /** Types into the text field: replaces the body and lets the ViewModel see the change. */
    protected fun TestScope.type(
        text: String
    ) {
        viewModel.buffer.edit { replace(start = 0, end = length, text = text) }
        Snapshot.sendApplyNotifications()
        runCurrent()
    }

    protected fun TestScope.changeOutside(
        text: String
    ) {
        store.texts[AREPAS] = text
        scanner.addNote(file = file(path = AREPAS.value, modifiedOffsetMillis = 1L))
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
        Snapshot.sendApplyNotifications()
        runCurrent()
    }

    protected fun TestScope.deleteOutside() {
        store.texts.remove(AREPAS)
        scanner.files.remove(AREPAS)
        advanceTimeBy(delayTimeMillis = TWO_SECONDS_MILLIS)
    }

    protected fun TestScope.collectEffects(): List<EditorEffect> {
        val effects = mutableListOf<EditorEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        return effects
    }

    protected fun putOnDisk(
        path: RelativePath,
        text: String
    ) {
        store.texts[path] = text
        scanner.addNote(file = file(path = path.value))
        operations.existing += path
    }

    companion object {
        const val TASKS_TEXT = "- [ ] Buy flour\n- [x] Soak beans\n"
        const val AREPAS_TEXT = "---\ntags: [recipe]\n---\n# Arepas\n"
        const val HALF_SECOND_MILLIS = 500L
        const val SECOND_MILLIS = 1_000L
        const val TWO_SECONDS_MILLIS = 2_000L
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val TASKS = RelativePath(value = "Tasks.md")
        val WELCOME = RelativePath(value = "Welcome.md")
    }
}
