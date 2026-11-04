package app.notekea.notes.presentation.mentions

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.data.JetBrainsMarkdownMentionFinder
import app.notekea.notes.domain.ApplyLinkUpdatesUseCase
import app.notekea.notes.domain.FakeNoteContentStore
import app.notekea.notes.domain.FindMentionsUseCase
import app.notekea.notes.domain.LinkMentionUseCase
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFixtures.entry
import app.notekea.notes.domain.NoteTextCache
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TextFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Linked and Unlinked (`ROADMAP.md` 2.5): they follow the open note and the vault's notes, and Link turns an unlinked
 * mention into a link, which moves it to Linked.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteMentionsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val contents = FakeNoteContentStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = NoteMentionsViewModel(
        vaultPath = VAULT,
        findMentions = FindMentionsUseCase(
            linkFinder = JetBrainsMarkdownLinkFinder(),
            mentionFinder = JetBrainsMarkdownMentionFinder()
        ),
        linkMention = LinkMentionUseCase(
            noteContentStore = contents,
            applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = contents)
        ),
        noteTextCache = NoteTextCache(contentStore = contents, textFolder = TextFolder()),
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `opening a note lists the notes that link to it and those that only name it`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = started()

        assertEquals(
            expected = listOf(WELCOME) to listOf(LISBON),
            actual = viewModel.state.value.mentions.let { found ->
                found.linked.map { it.source } to found.unlinked.map { it.source }
            }
        )
    }

    @Test
    fun `Link turns the mention into a link, and the vault changing moves the note to Linked`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = started()

        viewModel.onIntent(
            intent = NoteMentionsIntent.OnLink(mention = viewModel.state.value.mentions.unlinked.single())
        )
        advanceUntilIdle()
        viewModel.onIntent(intent = NoteMentionsIntent.OnNotesChange(notes = notes(changed = LISBON)))
        advanceUntilIdle()

        assertEquals(
            expected = Triple("We ate [[Arepas|arepas]] by the river.", listOf(LISBON, WELCOME), null),
            actual = Triple(
                contents.texts.getValue(LISBON),
                viewModel.state.value.mentions.linked.map { it.source },
                viewModel.state.value.linkFailed
            )
        )
    }

    @Test
    fun `a mention whose note changed meanwhile is not linked, and says so`() = runTest(context = testDispatcher) {
        val viewModel = started()
        val mention = viewModel.state.value.mentions.unlinked.single()
        contents.texts[LISBON] = "Another day, other plans."

        viewModel.onIntent(intent = NoteMentionsIntent.OnLink(mention = mention))
        advanceUntilIdle()

        assertEquals(
            expected = LISBON to "Another day, other plans.",
            actual = viewModel.state.value.linkFailed to contents.texts.getValue(LISBON)
        )
    }

    @Test
    fun `a search that fails says so and keeps what it found, and Try again finds it`() = runTest(
        context = testDispatcher
    ) {
        val viewModel = started()
        val found = viewModel.state.value.mentions
        contents.onRead = { error("The disk went away") }

        viewModel.onIntent(intent = NoteMentionsIntent.OnNotesChange(notes = notes(changed = LISBON)))
        advanceUntilIdle()
        val failed = viewModel.state.value
        contents.onRead = {}
        viewModel.onIntent(intent = NoteMentionsIntent.OnRetry)
        advanceUntilIdle()

        assertEquals(
            expected = Triple(true, found, false),
            actual = Triple(failed.searchFailed, failed.mentions, viewModel.state.value.searchFailed)
        )
    }

    private fun kotlinx.coroutines.test.TestScope.started(): NoteMentionsViewModel {
        contents.texts[AREPAS] = "# Arepas"
        contents.texts[WELCOME] = "Cook [[Arepas]] on Sundays."
        contents.texts[LISBON] = "We ate arepas by the river."
        val viewModel = viewModel()
        viewModel.onIntent(intent = NoteMentionsIntent.OnNotesChange(notes = notes()))
        viewModel.onIntent(intent = NoteMentionsIntent.OnNoteChange(note = AREPAS))
        advanceUntilIdle()
        return viewModel
    }

    /** The vault's notes; [changed] has a later date, as the tree has it after the file changed. */
    private fun notes(
        changed: RelativePath? = null
    ): Map<RelativePath, NoteEntry> = listOf(AREPAS, WELCOME, LISBON).associateWith { path ->
        entry(path = path.value, modifiedOffsetMillis = if (path == changed) CHANGE_MILLIS else 0L)
    }

    private companion object {
        const val VAULT = "/home/ana/Notes"
        const val CHANGE_MILLIS = 1_000L
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val WELCOME = RelativePath(value = "Welcome.md")
        val LISBON = RelativePath(value = "Travel/Lisbon trip.md")
    }
}
