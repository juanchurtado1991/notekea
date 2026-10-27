package app.notekea.notes.presentation.editor

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.viewModelScope
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.PropertyType
import app.notekea.notes.domain.PropertyValue
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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

/** The properties above a note (`ROADMAP.md` 1.14), edited over real use cases and fakes: saved at once, line by line. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorPropertiesTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fixture = EditorViewModels(dispatcher = testDispatcher)
    private val viewModel by lazy { fixture.create() }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        fixture.store.texts[NOTE] = TEXT
        fixture.scanner.addNote(file = file(path = NOTE.value))
        fixture.operations.existing += NOTE
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `each edit rewrites only its property's lines at once, keeping the text and the rest`() = runPropertiesTest {
        open()
        val opened = viewModel.state.value.properties
        send(intent = PropertyIntent.OnValueChange(key = "status", value = PropertyValue.Text(text = "Tested")))
        send(intent = PropertyIntent.OnAdd(type = PropertyType.CHECKBOX))
        send(intent = PropertyIntent.OnRename(from = "property", to = "favorite"))
        send(intent = PropertyIntent.OnRemove(key = "servings"))

        assertEquals(
            expected = listOf(
                "status" to PropertyValue.Text(text = "draft"),
                "status" to PropertyValue.Text(text = "Tested"),
                "favorite" to PropertyValue.Checkbox(isChecked = false)
            ) to "---\n# kept\nstatus: Tested\ntags: [recipe]\nfavorite: false\n---\n# Arepas\n",
            actual = listOf(
                (opened as FrontmatterContent.Properties).properties.first().let { it.key to it.value },
                properties().first().let { it.key to it.value },
                properties().last().let { it.key to it.value }
            ) to fixture.store.texts[NOTE]
        )
    }

    @Test
    fun `a taken or invalid name is not written, and the source replaces the frontmatter`() = runPropertiesTest {
        open()
        send(intent = PropertyIntent.OnRename(from = "status", to = "tags"))
        send(intent = PropertyIntent.OnRename(from = "status", to = "bad: name"))
        val unchanged = fixture.store.texts[NOTE]
        send(intent = PropertyIntent.OnSourceCommit(yaml = "title: Arepas\nstatus: done"))

        assertEquals(
            expected = TEXT to "---\ntitle: Arepas\nstatus: done\n---\n# Arepas\n",
            actual = unchanged to fixture.store.texts[NOTE]
        )
    }

    @Test
    fun `the fold is remembered for every note and run, and the source view is toggled`() = runPropertiesTest {
        open()
        send(intent = PropertyIntent.OnFoldToggle)
        send(intent = PropertyIntent.OnSourceToggle)

        assertEquals(
            expected = Triple(false, true, false),
            actual = Triple(
                viewModel.state.value.isPropertiesExpanded,
                viewModel.state.value.isSourceShown,
                fixture.preferences.isPropertiesExpanded
            )
        )
    }

    private fun properties() = (viewModel.state.value.properties as FrontmatterContent.Properties).properties

    private fun TestScope.open() {
        send(intent = EditorIntent.OnOpen(path = NOTE))
    }

    private fun TestScope.send(
        intent: EditorIntent
    ) {
        viewModel.onIntent(intent = intent)
        runCurrent()
        Snapshot.sendApplyNotifications()
        advanceTimeBy(delayTimeMillis = SETTLE_MILLIS)
        runCurrent()
    }

    private fun runPropertiesTest(
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
        private const val TEXT = "---\n# kept\nstatus: draft\ntags: [recipe]\nservings: 4\n---\n# Arepas\n"
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}
