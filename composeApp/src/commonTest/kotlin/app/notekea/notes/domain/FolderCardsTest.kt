package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.file
import kotlin.test.Test
import kotlin.test.assertEquals

class FolderCardsTest {
    @Test
    fun `a folder's grid holds what is directly inside it, subfolders first, then notes and files by name`() {
        val tree = listOf("Recipes/Pasta.md", "Recipes/Arepas.md", "Recipes/Drinks/Coffee.md", "Welcome.md")
            .fold(initial = VaultTree.EMPTY) { tree, path ->
                tree.withNote(note = NoteEntry(file = file(path = path), metadata = NoteMetadata.NONE))
            }
            .withAttachment(file = file(path = "Recipes/Menu 10.pdf"))
            .withAttachment(file = file(path = "Recipes/Menu 9.png"))
            .withFolder(path = RelativePath(value = "Recipes/Breakfast"))

        assertEquals(
            expected = listOf(
                "Recipes/Breakfast" to FolderCard.Kind.FOLDER,
                "Recipes/Drinks" to FolderCard.Kind.FOLDER,
                "Recipes/Arepas.md" to FolderCard.Kind.NOTE,
                "Recipes/Menu 9.png" to FolderCard.Kind.IMAGE,
                "Recipes/Menu 10.pdf" to FolderCard.Kind.FILE,
                "Recipes/Pasta.md" to FolderCard.Kind.NOTE
            ),
            actual = FolderCards(naturalNameComparator = NaturalNameComparator())
                .of(folder = RelativePath(value = "Recipes"), tree = tree)
                .map { it.path.value to it.kind }
        )
    }
}
