package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.notes.domain.LinkKind
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

class FollowLinkUseCaseTest {
    private val operations = FakeNoteFileOperations()
    private val followLink = FollowLinkUseCase(
        createNote = CreateNoteUseCase(noteFileOperations = operations),
        noteFileOperations = operations,
        validateFileName = ValidateFileNameUseCase()
    )
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
    fun `a bare wiki link to a missing note creates it at the vault root`() = runTest {
        val outcome = follow(target = "Café con leche", kind = LinkKind.WIKI, source = JOURNAL)

        assertEquals(
            expected = Outcome.Success(
                value = FollowedLink(path = RelativePath(value = "Café con leche.md"), isNew = true)
            ),
            actual = outcome
        )
    }

    // Audit: [[folder/New]] made New.md at the root, which never matched the link, so every click made another.
    @Test
    fun `a wiki link with a folder creates the note there, folders too, and then opens it`() = runTest {
        val created = follow(target = "Ideas/2026/Café", kind = LinkKind.WIKI)
        val path = RelativePath(value = "Ideas/2026/Café.md")

        val again = follow(target = "Ideas/2026/Café", kind = LinkKind.WIKI, notes = notes + path)

        assertEquals(
            expected = FollowedLink(path = path, isNew = true) to FollowedLink(path = path, isNew = false),
            actual = (created as Outcome.Success).value to (again as Outcome.Success).value
        )
        assertEquals(
            expected = setOf(RelativePath(value = "Ideas"), RelativePath(value = "Ideas/2026"), path),
            actual = operations.existing
        )
    }

    @Test
    fun `dots and a leading slash are read as paths, to open a note and to create one`() = runTest {
        val opened = listOf(
            follow(target = "./Arepas", kind = LinkKind.WIKI, source = "Recipes/Soup.md"),
            follow(target = "../Recipes/Arepas", kind = LinkKind.WIKI, source = JOURNAL),
            follow(target = "/Recipes/Arepas", kind = LinkKind.WIKI)
        )
        val created = listOf(
            follow(target = "./New", kind = LinkKind.WIKI, source = "Recipes/Soup.md"),
            follow(target = "../Other", kind = LinkKind.WIKI, source = "Recipes/Soup.md"),
            follow(target = "/Recipes/Rooted", kind = LinkKind.WIKI, source = JOURNAL)
        )

        assertEquals(
            expected = List(size = 3) { FollowedLink(path = RelativePath(value = AREPAS), isNew = false) },
            actual = opened.map { (it as Outcome.Success).value }
        )
        assertEquals(
            expected = listOf("Recipes/New.md", "Other.md", "Recipes/Rooted.md"),
            actual = created.map { (it as Outcome.Success).value.path.value }
        )
    }

    // Audit: [[..\\..\\evil]] created a file outside the vault: Okio takes `\\` for a separator and keeps `..`.
    @Test
    fun `a link that leaves the vault or cannot be a file creates nothing`() = runTest {
        val refused = listOf("..\\..\\evil", "../../evil", "a/../../../evil", "a:b", "folder\\evil").map {
            follow(target = it, kind = LinkKind.WIKI, source = JOURNAL)
        }

        assertEquals(expected = List(size = 5) { Outcome.Failure(error = NotesError.InvalidName) }, actual = refused)
        assertEquals(
            expected = Outcome.Failure(error = NotesError.HiddenName),
            actual = follow(target = ".obsidian/evil", kind = LinkKind.WIKI)
        )
        assertEquals(expected = emptySet(), actual = operations.existing)
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
