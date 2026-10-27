package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetNoteCoverUseCaseTest {
    private val store = FakeNoteContentStore()
    private val getNoteCover =
        GetNoteCoverUseCase(noteContentStore = store, vaultSettingsReader = FakeVaultSettingsReader())
    private val files = listOf("attachments/plate.png", "attachments/menu.pdf", "Recipes/corn.jpg").map(::RelativePath)

    @Test
    fun `a note's cover is the first image it embeds that exists, skipping other files and missing ones`() = runTest {
        val texts = listOf(
            "Intro\n![[menu.pdf]]\n![[gone.png]]\n![[plate.png|200]]\n![[corn.jpg]]",
            "![a corn field](corn.jpg)",
            "[[plate.png]] is only a link\n![[menu.pdf]]",
            "No pictures at all"
        )

        val covers = texts.map { text ->
            store.texts[NOTE] = text
            getNoteCover(vaultPath = VAULT, note = NOTE, files = files)?.value
        }

        assertEquals(
            expected = listOf("attachments/plate.png", "Recipes/corn.jpg", null, null),
            actual = covers
        )
    }

    companion object {
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}
