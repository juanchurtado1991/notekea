package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.viewModelScope
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
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

/** A template inserted in the open note (`ROADMAP.md` 1.12), over real use cases and fakes. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorTemplatesTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(dispatcher = testDispatcher)
    private val viewModel by lazy { fixture.create() }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        putOnDisk(path = AREPAS, text = AREPAS_TEXT)
        putOnDisk(path = EMPTY, text = "")
        putOnDisk(path = TEMPLATE, text = "---\ntags: [meal]\nsource: Grandma\n---\n## Notes for {{title}}, {{date}}\n")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `in read mode the request turns to edit, and the template goes in at the cursor with its properties saved`() =
        runTemplateTest {
            open(path = AREPAS)
            send(intent = EditorIntent.OnReadModeToggle)
            val effects = collectEffects()
            viewModel.buffer.edit { selection = TextRange(index = AREPAS_BODY.length) }

            send(intent = EditorIntent.OnTemplateRequest)
            // The picker takes the keyboard, which moves the text field's selection: the template still goes where it was.
            viewModel.buffer.edit { selection = TextRange(index = 0) }
            send(intent = EditorIntent.OnTemplateInsert(template = TEMPLATE))

            val body = "# Arepas\n\nMix.\n## Notes for Arepas, 2026-10-26\n"
            assertEquals(
                expected = listOf(
                    false,
                    body,
                    TextRange(index = body.length),
                    "---\ntags:\n  - recipe\n  - meal\nsource: Grandma\n---\n$body",
                    listOf(EditorEffect.RequestTemplate, EditorEffect.FocusText)
                ),
                actual = listOf(
                    viewModel.state.value.isReading,
                    viewModel.buffer.text.toString(),
                    viewModel.buffer.selection,
                    fixture.store.texts[AREPAS],
                    effects.toList()
                )
            )
        }

    @Test
    fun `an empty note becomes the template whole, its properties included`() = runTemplateTest {
        open(path = EMPTY)

        send(intent = EditorIntent.OnTemplateRequest)
        send(intent = EditorIntent.OnTemplateInsert(template = TEMPLATE))

        assertEquals(
            expected = "## Notes for Meeting, 2026-10-26\n" to
                "---\ntags: [meal]\nsource: Grandma\n---\n## Notes for Meeting, 2026-10-26\n",
            actual = viewModel.buffer.text.toString() to fixture.store.texts[EMPTY]
        )
    }

    @Test
    fun `a template that cannot be read says so and leaves the note as it was`() = runTemplateTest {
        open(path = AREPAS)
        val missing = RelativePath(value = "Templates/Gone.md")

        send(intent = EditorIntent.OnTemplateRequest)
        send(intent = EditorIntent.OnTemplateInsert(template = missing))

        assertEquals(
            expected = NotesError.EntryMissing(path = missing.value) to AREPAS_BODY,
            actual = viewModel.state.value.actionError to viewModel.buffer.text.toString()
        )
    }

    private fun TestScope.open(
        path: RelativePath
    ) {
        send(intent = EditorIntent.OnOpen(path = path))
    }

    /** Sends [intent], then lets the edits reach the ViewModel and its saves reach the disk. */
    private fun TestScope.send(
        intent: EditorIntent
    ) {
        viewModel.onIntent(intent = intent)
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

    private fun runTemplateTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    companion object {
        private const val AREPAS_BODY = "# Arepas\n\nMix.\n"
        private const val AREPAS_TEXT = "---\ntags: [recipe]\n---\n$AREPAS_BODY"
        private const val SETTLE_MILLIS = 400L
        private val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        private val EMPTY = RelativePath(value = "Meeting.md")
        private val TEMPLATE = RelativePath(value = "Templates/Recipe notes.md")
    }
}
