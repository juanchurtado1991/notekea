package app.notekea.chat.domain.skill

import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.noteTextCache
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What a vault tells the chat about itself (`docs/agent-loop.md`, step 4): its instructions note, and the person's
 * own skills beside the app's.
 */
class VaultGuideTest {
    private val store = FakeNoteContentStore()
    private val guide = VaultGuide(
        noteTextCache = noteTextCache(store = store)
    )

    private val instructions = RelativePath(value = "Notekea/Notekea.md")
    private val atRoot = RelativePath(value = "Notekea.md")
    private val recipe = RelativePath(value = "Notekea/Skills/Recipe card.md")
    private val summary = RelativePath(value = "Notekea/Skills/Document summary.md")
    private val empty = RelativePath(value = "Notekea/Skills/Empty.md")
    private val notes = listOf(instructions, recipe, summary, empty).map { entry(path = it.value) }

    @Test
    fun `the chat's memory is read from the Notekea folder, and is empty when the vault has none`() =
        runTest {
            val memory = RelativePath(value = "Notekea/Memory.md")
            store.texts[memory] = "## Areas\n- \"work\" is `Trabajo/`"

            val read = guide.memoryOf(
                vaultPath = VAULT,
                notes = notes + entry(path = memory.value)
            )

            assertEquals(
                expected = listOf(listOf("\"work\" is `Trabajo/`"), emptyList()),
                actual = listOf(
                    read.map { it.text },
                    guide.memoryOf(VAULT, notes)
                )
            )
        }

    @Test
    fun `the vault's instructions are its note's text, and none when it has no such note or it is empty`() =
        runTest {
            store.texts[instructions] = "New notes go in Inbox. Write in Spanish."
            val read = guide.instructionsOf(VAULT, notes)

            val missing = guide.instructionsOf(
                vaultPath = VAULT,
                notes = notes.drop(n = 1)
            )

            store.texts[empty] = "  \n"

            val blank = guide.instructionsOf(
                vaultPath = VAULT,
                notes = notes,
                note = empty
            )

            assertEquals(
                expected = listOf("New notes go in Inbox. Write in Spanish.", null, null),
                actual = listOf(read, missing, blank)
            )
        }

    @Test
    fun `the vault's instructions are read from the Notekea folder, not from a note of that name at the root`() =
        runTest {
            store.texts[atRoot] = "Write in English."

            val read = guide.instructionsOf(
                vaultPath = VAULT,
                notes = notes.drop(n = 1) + entry(path = atRoot.value)
            )

            assertEquals(
                expected = null,
                actual = read
            )
        }

    @Test
    fun `the person's skills are the notes of the folder, beside the app's, and one of the same name replaces it`() =
        runTest {
            store.texts[recipe] = "# Write a recipe as a card\nIngredients first, as a list.\nThen the steps."
            store.texts[summary] = "Summaries my way\nThree bullets, no more."
            store.texts[empty] = ""

            val skills = guide.skillsOf(
                vaultPath = VAULT,
                notes = notes
            )

            assertEquals(
                expected = listOf(
                    listOf("comparison table", "study cards", "meeting notes", "document summary", "recipe card"),
                    Skill(
                        name = "recipe card",
                        description = "Write a recipe as a card",
                        instructions = "Ingredients first, as a list.\nThen the steps."
                    ),
                    "Three bullets, no more."
                ),
                actual = listOf(
                    skills.map { it.name },
                    skills.last(),
                    skills.first {
                        it.name == "document summary"
                    }.instructions
                )
            )
        }
}
