package app.notekea.notes.presentation.tree

import kotlinx.coroutines.ExperimentalCoroutinesApi
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteRewrite
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.TrashLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent

/** After a rename, the links to the entry are offered and written only on Update links (`ROADMAP.md` 2.5). */
@OptIn(ExperimentalCoroutinesApi::class)
class VaultTreeLinkUpdateTest : VaultTreeViewModelTestBase() {
    @Test
    fun `renaming a linked note asks first, then updates the notes that link to it and says so`() = runTreeTest {
        contents.texts[path(ROADMAP)] = "Begin with [[Welcome]]."
        val viewModel = startedViewModel()
        rename(viewModel = viewModel, from = WELCOME, to = "Start here")
        val asked = viewModel.state.value.linkOffer?.let { listOf(it.linkCount, it.noteCount) }
        val before = contents.texts.getValue(path(ROADMAP))

        viewModel.onIntent(intent = VaultTreeIntent.OnLinkUpdateConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(
                listOf(1, 1),
                "Begin with [[Welcome]].",
                "Begin with [[Start here]].",
                TreeNotice.LinksUpdated(notes = listOf("Roadmap"), skippedCount = 0),
                null
            ),
            actual = listOf(
                asked,
                before,
                contents.texts.getValue(path(ROADMAP)),
                viewModel.state.value.notice,
                viewModel.state.value.linkOffer
            )
        )
    }

    @Test
    fun `declining the offer keeps the old links and says nothing`() = runTreeTest {
        contents.texts[path(ROADMAP)] = "Begin with [[Welcome]]."
        val viewModel = startedViewModel()
        rename(viewModel = viewModel, from = WELCOME, to = "Start here")

        viewModel.onIntent(intent = VaultTreeIntent.OnLinkUpdateDecline)
        advanceUntilIdle()

        assertEquals(
            expected = listOf("Begin with [[Welcome]].", null, null),
            actual = listOf(
                contents.texts.getValue(path(ROADMAP)),
                viewModel.state.value.notice,
                viewModel.state.value.linkOffer
            )
        )
    }

    @Test
    fun `a shown links notice never blocks the next rename, its links or its notice`() = runTreeTest {
        contents.texts[path(ROADMAP)] = "Begin with [[Welcome]]."
        val viewModel = startedViewModel()
        rename(viewModel = viewModel, from = WELCOME, to = "Start here")
        viewModel.onIntent(intent = VaultTreeIntent.OnLinkUpdateConfirm)
        advanceUntilIdle()
        val first = viewModel.state.value.notice

        rename(viewModel = viewModel, from = "Start here.md", to = "Begin")
        viewModel.onIntent(intent = VaultTreeIntent.OnLinkUpdateConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = Triple(
                TreeNotice.LinksUpdated(notes = listOf("Roadmap"), skippedCount = 0),
                "Begin with [[Begin]].",
                TreeNotice.LinksUpdated(notes = listOf("Roadmap"), skippedCount = 0)
            ),
            actual = Triple(first, contents.texts.getValue(path(ROADMAP)), viewModel.state.value.notice)
        )
    }

    @Test
    fun `renaming a note nothing links to changes no other note and says nothing`() = runTreeTest {
        contents.texts[path(ROADMAP)] = "No links here."
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameRequest(path = path(WELCOME)))
        viewModel.onIntent(intent = VaultTreeIntent.OnRenameTextChange(text = "Start here"))

        viewModel.onIntent(intent = VaultTreeIntent.OnRenameConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = Triple("No links here.", null, null),
            actual = Triple(
                contents.texts.getValue(path(ROADMAP)),
                viewModel.state.value.notice,
                viewModel.state.value.linkOffer
            )
        )
    }

    @Test
    fun `a rename from the editor selects the note at its new path and offers its links`() = runTreeTest {
        contents.texts[path(ROADMAP)] = "Begin with [[Welcome]]."
        val viewModel = startedViewModel()
        viewModel.onIntent(intent = VaultTreeIntent.OnRowClick(path = path(WELCOME)))
        val rewrite = NoteRewrite(
            path = path(ROADMAP),
            original = "Begin with [[Welcome]].",
            updated = "Begin with [[Start here]].",
            linkCount = 1
        )

        viewModel.onIntent(
            intent = VaultTreeIntent.OnEntryRenamed(
                from = path(WELCOME),
                to = path("Start here.md"),
                rewrites = listOf(rewrite)
            )
        )
        advanceUntilIdle()
        val offer = viewModel.state.value.linkOffer
        viewModel.onIntent(intent = VaultTreeIntent.OnLinkUpdateConfirm)
        advanceUntilIdle()

        assertEquals(
            expected = listOf(
                path("Start here.md"),
                LinkUpdateOffer(from = path(WELCOME), to = path("Start here.md"), rewrites = listOf(rewrite)),
                TreeNotice.LinksUpdated(notes = listOf("Roadmap"), skippedCount = 0),
                "Begin with [[Start here]]."
            ),
            actual = listOf(
                viewModel.state.value.selectedPath,
                offer,
                viewModel.state.value.notice,
                contents.texts.getValue(path(ROADMAP))
            )
        )
        assertTrue(actual = path("Start here.md") in viewModel.state.value.tree.notes)
    }
}
