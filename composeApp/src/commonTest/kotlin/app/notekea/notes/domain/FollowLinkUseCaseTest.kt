package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FollowLinkUseCaseTest {
    private val operations = FakeNoteFileOperations()
    private val followLink = FollowLinkUseCase(createNote = CreateNoteUseCase(noteFileOperations = operations))
    private val notes = setOf(AREPAS, WELCOME, JOURNAL).map { RelativePath(value = it) }.toSet()

    @Test
    fun `a wiki link opens the note it names, ignoring letter case`() = runTest {
        assertEquals(
            expected = Outcome.Success(value = FollowedLink(path = RelativePath(value = AREPAS), isNew = false)),
            actual = follow(target = "arepas", kind = LinkKind.WIKI)
        )
    }

    @Test
    fun `a markdown link is read from the linking note's folder, then from the vault root`() = runTest {
        assertEquals(
            expected = listOf(RelativePath(value = AREPAS), RelativePath(value = AREPAS)),
            actual = listOf(
                follow(target = "../Recipes/Arepas.md", kind = LinkKind.MARKDOWN_ANGLE, source = JOURNAL),
                follow(target = "Recipes/Arepas.md", kind = LinkKind.MARKDOWN_ANGLE, source = JOURNAL)
            ).map { (it as Outcome.Success).value.path }
        )
    }

    @Test
    fun `a name two notes share opens the closest one and creates nothing`() = runTest {
        val shared = setOf(SOUP_IN_RECIPES, SOUP_IN_ARCHIVE, SOUP_IN_TRAVEL, WELCOME, JOURNAL).map {
            RelativePath(value = it)
        }.toSet()

        // From the vault root and from Journal the shallowest wins (Recipes before Travel by name); from Archive, its own.
        assertEquals(
            expected = listOf(SOUP_IN_RECIPES, SOUP_IN_ARCHIVE, SOUP_IN_RECIPES).map {
                Outcome.Success(value = FollowedLink(path = RelativePath(value = it), isNew = false))
            },
            actual = listOf(WELCOME, "Projects/Archive/Ideas.md", JOURNAL).map { source ->
                follow(target = "Pasta e fagioli", kind = LinkKind.WIKI, source = source, notes = shared)
            }
        )
        assertEquals(
            expected = emptySet(),
            actual = operations.existing
        )
    }

    @Test
    fun `a wiki link to a missing note creates it at the vault root`() = runTest {
        val outcome = follow(target = "Ideas/Café con leche", kind = LinkKind.WIKI)

        assertEquals(
            expected = Outcome.Success(
                value = FollowedLink(path = RelativePath(value = "Café con leche.md"), isNew = true)
            ),
            actual = outcome
        )
        assertEquals(
            expected = true,
            actual = RelativePath(value = "Café con leche.md") in operations.existing
        )
    }

    @Test
    fun `a markdown link to a missing file is reported, nothing is created`() = runTest {
        assertEquals(
            expected = Outcome.Failure(error = NotesError.EntryMissing(path = "Missing.md")),
            actual = follow(target = "Missing.md", kind = LinkKind.MARKDOWN_ANGLE)
        )
        assertEquals(
            expected = emptySet(),
            actual = operations.existing
        )
    }

    private suspend fun follow(
        target: String,
        kind: LinkKind,
        source: String = WELCOME,
        notes: Set<RelativePath> = this.notes
    ): Outcome<FollowedLink, NotesError> = followLink(
        vaultPath = VAULT,
        notes = notes,
        source = RelativePath(value = source),
        target = target,
        kind = kind
    )

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val JOURNAL = "Journal/2026-10-20.md"
        private const val SOUP_IN_ARCHIVE = "Projects/Archive/Pasta e fagioli.md"
        private const val SOUP_IN_RECIPES = "Recipes/Pasta e fagioli.md"
        private const val SOUP_IN_TRAVEL = "Travel/Pasta e fagioli.md"
        private const val WELCOME = "Welcome.md"
    }
}
