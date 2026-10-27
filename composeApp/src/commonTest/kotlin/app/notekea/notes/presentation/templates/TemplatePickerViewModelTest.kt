package app.notekea.notes.presentation.templates

import androidx.lifecycle.viewModelScope
import app.notekea.notes.domain.CreateTemplatesFolderUseCase
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FakeNoteFileOperations
import app.notekea.notes.domain.FakeVaultSettingsReader
import app.notekea.notes.domain.FuzzyMatcher
import app.notekea.notes.domain.GetTemplateSettingsUseCase
import app.notekea.notes.domain.NoteFixtures.VAULT
import app.notekea.notes.domain.NoteFixtures.entry
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TemplateSettings
import app.notekea.notes.domain.TextFolder
import app.notekea.notes.domain.VaultTree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The template picker (`ROADMAP.md` 1.12) over real use cases and fakes. */
@OptIn(ExperimentalCoroutinesApi::class)
class TemplatePickerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = FakeVaultSettingsReader()
    private val operations = FakeNoteFileOperations()
    private val store = FakeNoteContentStore()
    private val viewModel by lazy {
        TemplatePickerViewModel(
            vaultPath = VAULT,
            getTemplateSettings = GetTemplateSettingsUseCase(vaultSettingsReader = settings),
            createTemplatesFolder = CreateTemplatesFolderUseCase(
                vaultSettingsReader = settings,
                noteFileOperations = operations,
                noteContentStore = store
            ),
            fuzzyMatcher = FuzzyMatcher(textFolder = TextFolder())
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `it lists the folder's templates, typing finds them by name, and Enter chooses one for its target`() =
        runPickerTest {
            val effects = collectEffects()
            send(intent = TemplatePickerIntent.OnTreeChange(tree = TREE))
            send(intent = TemplatePickerIntent.OnOpen(target = TemplateTarget.NewNote(folder = PROJECTS)))
            val all = names()
            send(intent = TemplatePickerIntent.OnQueryChange(query = "wkr"))
            val found = names()
            send(intent = TemplatePickerIntent.OnConfirm)

            assertEquals(
                expected = listOf(
                    listOf("Meeting notes", "Work/Weekly review"),
                    listOf("Work/Weekly review"),
                    listOf(
                        TemplatePickerEffect.TemplateChosen(
                            template = RelativePath(value = "Templates/Work/Weekly review.md"),
                            target = TemplateTarget.NewNote(folder = PROJECTS)
                        )
                    ),
                    false
                ),
                actual = listOf(all, found, effects.toList(), viewModel.state.value.isOpen)
            )
        }

    @Test
    fun `the vault's settings name the folder, read again at every opening`() = runPickerTest {
        send(intent = TemplatePickerIntent.OnTreeChange(tree = TREE))
        settings.templates = TemplateSettings.DEFAULT.copy(folder = "Snippets")
        send(intent = TemplatePickerIntent.OnOpen(target = TemplateTarget.CurrentNote))

        assertEquals(
            expected = Triple("Snippets", false, emptyList()),
            actual = Triple(viewModel.state.value.folder, viewModel.state.value.hasFolder, names())
        )
    }

    @Test
    fun `without a templates folder, Create makes it with the example, which is listed at once`() = runPickerTest {
        send(intent = TemplatePickerIntent.OnTreeChange(tree = VaultTree.EMPTY))
        send(intent = TemplatePickerIntent.OnOpen(target = TemplateTarget.CurrentNote))
        val before = viewModel.state.value.hasFolder
        send(
            intent = TemplatePickerIntent.OnCreateFolderClick(
                exampleName = "Meeting notes",
                exampleText = "# {{title}}"
            )
        )

        assertEquals(
            expected = listOf(false, true, listOf("Meeting notes"), "# {{title}}"),
            actual = listOf(
                before,
                viewModel.state.value.hasFolder,
                names(),
                store.texts[RelativePath(value = "Templates/Meeting notes.md")]
            )
        )
    }

    @Test
    fun `closing without choosing says so, and an empty folder or a search matching nothing is told apart`() =
        runPickerTest {
            val effects = collectEffects()
            send(intent = TemplatePickerIntent.OnTreeChange(tree = VaultTree.EMPTY.withFolder(path = TEMPLATES)))
            send(intent = TemplatePickerIntent.OnOpen(target = TemplateTarget.CurrentNote))
            val empty = viewModel.state.value.hasFolder to viewModel.state.value.hasTemplates
            send(intent = TemplatePickerIntent.OnTreeChange(tree = TREE))
            send(intent = TemplatePickerIntent.OnQueryChange(query = "zzz"))
            val noMatch = viewModel.state.value.hasTemplates to names()
            send(intent = TemplatePickerIntent.OnClose)

            assertEquals(
                expected = listOf(true to false, true to emptyList<String>(), listOf(TemplatePickerEffect.Dismissed)),
                actual = listOf(empty, noMatch, effects.toList())
            )
        }

    private fun names(): List<String> = viewModel.state.value.rows.map { it.name }

    private fun TestScope.send(
        intent: TemplatePickerIntent
    ) {
        viewModel.onIntent(intent = intent)
        advanceUntilIdle()
        // The effects' collector runs in the background scope, which waiting until idle does not wait for.
        runCurrent()
    }

    private fun TestScope.collectEffects(): List<TemplatePickerEffect> {
        val effects = mutableListOf<TemplatePickerEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        runCurrent()
        return effects
    }

    private fun runPickerTest(
        body: suspend TestScope.() -> Unit
    ) = runTest(context = testDispatcher) {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    companion object {
        private val PROJECTS = RelativePath(value = "Projects")
        private val TEMPLATES = RelativePath(value = "Templates")
        private val TREE = listOf("Templates/Meeting notes.md", "Templates/Work/Weekly review.md", "Projects/Plan.md")
            .fold(initial = VaultTree.EMPTY) { tree, path -> tree.withNote(note = entry(path = path)) }
    }
}
