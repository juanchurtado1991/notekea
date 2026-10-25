package app.notekea.notes.domain

import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UpdateLinksAfterRenameUseCaseTest {
    private val store = FakeNoteContentStore()
    private val updateLinksAfterRename = UpdateLinksAfterRenameUseCase(
        planLinkUpdates = PlanLinkUpdatesUseCase(noteContentStore = store, linkFinder = JetBrainsMarkdownLinkFinder()),
        applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)
    )

    @Test
    fun `the notes that link to the renamed entry are rewritten and counted`() = runTest {
        store.texts[RelativePath(value = WELCOME)] = "Cook [[Arepas]]."
        store.texts[RelativePath(value = JOURNAL)] = "Ate [[Arepas]] again."

        val result = rename()

        assertEquals(
            expected = LinkUpdateResult(
                updated = listOf(RelativePath(value = WELCOME), RelativePath(value = JOURNAL)),
                skipped = emptyList()
            ),
            actual = result
        )
        assertEquals(
            expected = listOf("Cook [[Arepas reina]].", "Ate [[Arepas reina]] again."),
            actual = listOf(WELCOME, JOURNAL).map { store.texts.getValue(RelativePath(value = it)) }
        )
    }

    @Test
    fun `nothing is reported when no note links to the renamed entry`() = runTest {
        store.texts[RelativePath(value = WELCOME)] = "No links here."
        store.texts[RelativePath(value = JOURNAL)] = "Nor here."

        assertNull(actual = rename())
        assertEquals(
            expected = "No links here.",
            actual = store.texts.getValue(RelativePath(value = WELCOME))
        )
    }

    private suspend fun rename(): LinkUpdateResult? = updateLinksAfterRename(
        vaultPath = VAULT,
        notesBefore = setOf(AREPAS, WELCOME, JOURNAL).map { RelativePath(value = it) }.toSet(),
        from = RelativePath(value = AREPAS),
        to = RelativePath(value = RENAMED)
    )

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val JOURNAL = "Journal/2026-10-20.md"
        private const val RENAMED = "Recipes/Arepas reina.md"
        private const val WELCOME = "Welcome.md"
    }
}
