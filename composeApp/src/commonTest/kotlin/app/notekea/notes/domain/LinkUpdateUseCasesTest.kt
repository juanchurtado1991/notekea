package app.notekea.notes.domain

import app.notekea.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkUpdateUseCasesTest {
    private val store = FakeNoteContentStore()
    private val planLinkUpdates =
        PlanLinkUpdatesUseCase(noteContentStore = store, linkFinder = JetBrainsMarkdownLinkFinder())
    private val applyLinkUpdates = ApplyLinkUpdatesUseCase(noteContentStore = store)

    @Test
    fun `bare links follow a renamed note and keep their alias, heading, embed and extension`() = runTest {
        val rewrites =
            renameArepas(source = "Try [[Arepas]], [[arepas|the arepas]], ![[Arepas#Dough]] and [[Arepas.md]].")

        assertEquals(
            expected = "Try [[Arepas reina]], [[Arepas reina|the arepas]], ![[Arepas reina#Dough]] " +
                "and [[Arepas reina.md]].",
            actual = rewrites.single().updated
        )
        assertEquals(
            expected = 4,
            actual = rewrites.single().linkCount
        )
    }

    @Test
    fun `path links stay paths and markdown links keep their base and encoding`() = runTest {
        val rewrites = renameArepas(
            source = "[[Recipes/Arepas]] [x](../Recipes/Arepas.md) [y](<Recipes/Arepas.md>) " +
                "[z](Recipes/Arepas.md#Dough)",
            sourcePath = "Journal/2026-10-20.md"
        )

        assertEquals(
            expected = "[[Recipes/Arepas reina]] [x](../Recipes/Arepas%20reina.md) [y](<Recipes/Arepas reina.md>) " +
                "[z](Recipes/Arepas%20reina.md#Dough)",
            actual = rewrites.single().updated
        )
    }

    @Test
    fun `a new name shared by another note is written as a path`() = runTest {
        store.texts[path("Ideas/Arepas reina.md")] = "# Another note with that name"

        val rewrites = renameArepas(source = "[[Arepas]]", others = setOf(path("Ideas/Arepas reina.md")))

        assertEquals(
            expected = "[[Recipes/Arepas reina]]",
            actual = rewrites.single().updated
        )
    }

    @Test
    fun `code, other notes and ambiguous names are left alone`() = runTest {
        store.texts[path("Ideas/Arepas.md")] = "# Ideas about arepas"
        val notes = setOf(path(AREPAS), path("Ideas/Arepas.md"), path(SOURCE))
        store.texts[path(SOURCE)] = "`[[Recipes/Arepas]]` [[Pasta e fagioli]] [[Arepas]]"

        val rewrites = planLinkUpdates(vaultPath = VAULT, notesBefore = notes, from = path(AREPAS), to = path(RENAMED))

        assertEquals(
            expected = emptyList(),
            actual = rewrites
        )
    }

    @Test
    fun `a renamed folder rebases path links and leaves bare ones`() = runTest {
        val notes = setOf(path(AREPAS), path(SOURCE))
        store.texts[path(SOURCE)] = "[[Recipes/Arepas]] [[Arepas]] [x](Recipes/Arepas.md)"

        val rewrites =
            planLinkUpdates(vaultPath = VAULT, notesBefore = notes, from = path("Recipes"), to = path("Cooking"))

        assertEquals(
            expected = "[[Cooking/Arepas]] [[Arepas]] [x](Cooking/Arepas.md)",
            actual = rewrites.single().updated
        )
    }

    @Test
    fun `a note's links to itself follow it, read from its new path`() = runTest {
        store.texts[path(RENAMED)] = "Back to [[#Dough]] and [[Arepas#Filling]]"

        val rewrites = planLinkUpdates(
            vaultPath = VAULT,
            notesBefore = setOf(path(AREPAS)),
            from = path(AREPAS),
            to = path(RENAMED)
        )

        assertEquals(
            expected = path(RENAMED) to "Back to [[#Dough]] and [[Arepas reina#Filling]]",
            actual = rewrites.single().let { it.path to it.updated }
        )
    }

    @Test
    fun `only notes unchanged since planning are written`() = runTest {
        val rewrites = listOf("A.md", "B.md", "C.md").map { name ->
            store.texts[path(name)] = "[[Arepas]]"
            NoteRewrite(path = path(name), original = "[[Arepas]]", updated = "[[Arepas reina]]", linkCount = 1)
        }
        store.texts[path("B.md")] = "[[Arepas]] edited meanwhile"
        store.refused += path("C.md")

        val result = applyLinkUpdates(vaultPath = VAULT, rewrites = rewrites)

        assertEquals(
            expected = LinkUpdateResult(updatedCount = 1, skipped = listOf(path("B.md"), path("C.md"))),
            actual = result
        )
        assertEquals(
            expected = listOf("[[Arepas reina]]", "[[Arepas]] edited meanwhile", "[[Arepas]]"),
            actual = listOf("A.md", "B.md", "C.md").map { store.texts.getValue(path(it)) }
        )
    }

    private suspend fun renameArepas(
        source: String,
        sourcePath: String = SOURCE,
        others: Set<RelativePath> = emptySet()
    ): List<NoteRewrite> {
        store.texts[path(sourcePath)] = source
        return planLinkUpdates(
            vaultPath = VAULT,
            notesBefore = setOf(path(AREPAS), path(sourcePath)) + others,
            from = path(AREPAS),
            to = path(RENAMED)
        )
    }

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val RENAMED = "Recipes/Arepas reina.md"
        private const val SOURCE = "Welcome.md"
    }
}
