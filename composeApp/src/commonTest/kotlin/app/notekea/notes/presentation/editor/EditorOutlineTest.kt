package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.RelativePath
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The open note's outline (`ROADMAP.md` 1.9), over real use cases and fakes, and the side panel's floating state. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorOutlineTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(dispatcher = testDispatcher)
    private val viewModel by lazy { fixture.create() }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        putOnDisk(path = AREPAS, text = "---\ntags: [recipe]\n---\n# Arepas\n\n## Dough\nMix.\n")
        putOnDisk(path = PLAIN, text = "No headings here.\n")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the outline lists the body's headings and follows typing, and another note brings its own`() = runOutlineTest {
        open(path = AREPAS)
        val opened = viewModel.state.value.outline
        type(text = "# Arepas\n\n## Dough\nMix.\n### Rest\n")
        val typed = viewModel.state.value.outline.map { it.text }
        open(path = PLAIN)

        assertEquals(
            expected = Triple(
                listOf(
                    OutlineHeading(level = 1, text = "Arepas", offset = 0, depth = 0),
                    OutlineHeading(level = 2, text = "Dough", offset = 10, depth = 1)
                ),
                listOf("Arepas", "Dough", "Rest"),
                emptyList()
            ),
            actual = Triple(opened, typed, viewModel.state.value.outline)
        )
    }

    @Test
    fun `choosing a heading in edit mode puts the cursor at its end, scrolls to it and gives the text the keyboard`() =
        runOutlineTest {
            open(path = AREPAS)
            val effects = collectEffects()

            send(intent = EditorIntent.OnHeadingClick(index = 1))

            assertEquals(
                expected = TextRange(index = 18) to listOf(
                    EditorEffect.RevealHeading(heading = viewModel.state.value.outline[1]),
                    EditorEffect.FocusText
                ),
                actual = viewModel.buffer.selection to effects.toList()
            )
        }

    @Test
    fun `choosing a heading in read mode only scrolls to it, and a heading that is gone does nothing`() =
        runOutlineTest {
            open(path = AREPAS)
            send(intent = EditorIntent.OnReadModeToggle)
            val effects = collectEffects()

            send(intent = EditorIntent.OnHeadingClick(index = 0))
            send(intent = EditorIntent.OnHeadingClick(index = 5))

            assertEquals(
                expected = TextRange(index = 0) to listOf<EditorEffect>(
                    EditorEffect.RevealHeading(heading = viewModel.state.value.outline[0])
                ),
                actual = viewModel.buffer.selection to effects.toList()
            )
        }

    @Test
    fun `going back to a note puts the cursor where it was left`() = runOutlineTest {
        open(path = AREPAS)
        viewModel.buffer.edit { selection = TextRange(start = 2, end = 8) }
        open(path = PLAIN)
        val plain = viewModel.buffer.selection
        open(path = AREPAS)

        assertEquals(
            expected = TextRange(index = 0) to TextRange(start = 2, end = 8),
            actual = plain to viewModel.buffer.selection
        )
    }

    @Test
    fun `while the panel floats over a narrow window, its button opens and closes it there only`() {
        val sent = mutableListOf<EditorIntent>()
        val host = SidePanelHost(send = { sent += it })

        host.onIntent(intent = EditorIntent.OnSidePanelToggle)
        host.isOverlay = true
        host.onIntent(intent = EditorIntent.OnSidePanelToggle)
        val opened = host.isOverlayOpen
        host.onIntent(intent = EditorIntent.OnSidePanelToggle)
        host.onIntent(intent = EditorIntent.OnSaveShortcut)

        assertEquals(
            expected = Triple(
                true,
                false,
                listOf<EditorIntent>(EditorIntent.OnSidePanelToggle, EditorIntent.OnSaveShortcut)
            ),
            actual = Triple(opened, host.isOverlayOpen, sent.toList())
        )
    }

    private fun TestScope.open(
        path: RelativePath
    ) {
        send(intent = EditorIntent.OnOpen(path = path))
    }

    private fun TestScope.type(
        text: String
    ) {
        viewModel.buffer.edit { replace(start = 0, end = length, text = text) }
        settle()
    }

    /** Sends [intent], then lets the edits reach the ViewModel and the outline be found again. */
    private fun TestScope.send(
        intent: EditorIntent
    ) {
        viewModel.onIntent(intent = intent)
        settle()
    }

    private fun TestScope.settle() {
        runCurrent()
        Snapshot.sendApplyNotifications()
        advanceTimeBy(delayTimeMillis = SETTLE_MILLIS)
        runCurrent()
    }

    private fun TestScope.collectEffects(): List<EditorEffect> {
        val effects = mutableListOf<EditorEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        runCurrent()
        return effects
    }

    private fun putOnDisk(
        path: RelativePath,
        text: String
    ) {
        fixture.store.texts[path] = text
        fixture.scanner.addNote(file = file(path = path.value))
        fixture.operations.existing += path
    }

    private fun runOutlineTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    companion object {
        private const val SETTLE_MILLIS = 400L
        private val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        private val PLAIN = RelativePath(value = "Plain.md")
    }
}
