package app.notekea.editor.presentation.sidepanel

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import app.notekea.core.notes.domain.NoteFixtures.file
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.viewmodel.EditorViewModels
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

/** The open note's outline, over real use cases and fakes, and the side panel's floating state. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorOutlineTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(dispatcher = testDispatcher)
    private val viewModel by lazy { fixture.create() }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)

        putOnDisk(
            path = AREPAS,
            text = "---\ntags: [recipe]\n---\n# Arepas\n\n## Dough\nMix.\n"
        )

        putOnDisk(
            path = PLAIN,
            text = "No headings here.\n"
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the outline lists the body's headings and follows typing, and another note brings its own`() =
        runOutlineTest {
            open(path = AREPAS)
            val opened = viewModel.state.value.outline
            type(text = "# Arepas\n\n## Dough\nMix.\n### Rest\n")

            val typed = viewModel
                .state
                .value
                .outline
                .map { it.text }

            open(path = PLAIN)

            assertEquals(
                expected = Triple(
                    listOf(
                        OutlineHeading(
                            level = 1,
                            text = "Arepas",
                            offset = 0,
                            depth = 0
                        ),
                        OutlineHeading(
                            level = 2,
                            text = "Dough",
                            offset = 10,
                            depth = 1
                        )
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
    fun `choosing a heading that is gone does nothing`() = runOutlineTest {
        open(path = AREPAS)
        val effects = collectEffects()

        send(intent = EditorIntent.OnHeadingClick(index = 5))

        assertEquals(
            expected = TextRange(index = 0) to emptyList<EditorEffect>(),
            actual = viewModel.buffer.selection to effects.toList()
        )
    }

    @Test
    fun `a mention chosen in Connections is selected once its note opens, or at once in the open note`() =
        runOutlineTest {
            open(path = PLAIN)

            send(
                intent = EditorIntent.OnRevealRequest(
                    path = AREPAS,
                    range = 10..17
                )
            )

            val waiting = viewModel.buffer.selection
            open(path = AREPAS)
            val opened = viewModel.buffer.selection

            send(
                intent = EditorIntent.OnRevealRequest(
                    path = AREPAS,
                    range = 2..7
                )
            )

            assertEquals(
                expected = Triple(
                    TextRange(index = 0),
                    TextRange(
                        start = 10,
                        end = 18
                    ),
                    TextRange(
                        start = 2,
                        end = 8
                    )
                ),
                actual = Triple(waiting, opened, viewModel.buffer.selection)
            )
        }

    // The developer's request of 2026-11-14: a source clicked in the chat selected its passage and put its lines
    // into editing, their Markdown marks shown.
    @Test
    fun `a passage only shown is selected without the keyboard, and marked so its lines are not edited`() =
        runOutlineTest {
            open(path = AREPAS)
            val effects = collectEffects()

            send(
                intent = EditorIntent.OnRevealRequest(
                    path = AREPAS,
                    range = 2..7,
                    isShownOnly = true
                )
            )

            val shown = viewModel.state.value.shownPassage
            val quiet = effects.toList()

            send(
                intent = EditorIntent.OnRevealRequest(
                    path = AREPAS,
                    range = 2..7
                )
            )

            assertEquals(
                expected = listOf<Any?>(
                    TextRange(
                        start = 2,
                        end = 8
                    ),
                    2..7,
                    emptyList<EditorEffect>(),
                    null,
                    true
                ),
                actual = listOf<Any?>(
                    viewModel.buffer.selection,
                    shown,
                    quiet,
                    viewModel.state.value.shownPassage,
                    effects.isNotEmpty()
                )
            )
        }

    @Test
    fun `going back to a note puts the cursor where it was left`() = runOutlineTest {
        open(path = AREPAS)

        viewModel.buffer.edit {
            selection = TextRange(
                start = 2,
                end = 8
            )
        }

        open(path = PLAIN)
        val plain = viewModel.buffer.selection
        open(path = AREPAS)

        assertEquals(
            expected = TextRange(index = 0) to TextRange(
                start = 2,
                end = 8
            ),
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

    private fun TestScope.open(path: RelativePath) {
        send(intent = EditorIntent.OnOpen(path = path))
    }

    private fun TestScope.type(text: String) {
        viewModel.buffer.edit {
            replace(
                start = 0,
                end = length,
                text = text
            )
        }

        settle()
    }

    /** Sends [intent], then lets the edits reach the ViewModel and the outline be found again. */
    private fun TestScope.send(intent: EditorIntent) {
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

        backgroundScope.launch {
            viewModel.effects.collect { effects += it }
        }

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

    private fun runOutlineTest(body: suspend TestScope.() -> Unit) = runTest(
        context = testDispatcher
    ) {
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
