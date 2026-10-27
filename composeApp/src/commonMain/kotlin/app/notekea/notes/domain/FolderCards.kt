package app.notekea.notes.domain

/**
 * What a folder's grid shows (`ROADMAP.md` 1.11): what is directly inside [folder] in [tree], subfolders first, then
 * notes and files together, each group in the tree's name order (notes by their name without `.md`).
 */
class FolderCards(private val naturalNameComparator: NaturalNameComparator) {
    fun of(
        folder: RelativePath,
        tree: VaultTree
    ): List<FolderCard> {
        val folders = tree.folders.filter { it.parent == folder && !it.isRoot }
            .sortedWith(comparator = compareBy(comparator = naturalNameComparator) { it.name })
            .map { FolderCard(path = it, kind = FolderCard.Kind.FOLDER) }
        val notes = tree.notes.keys.filter { it.parent == folder }
            .map { FolderCard(path = it, kind = FolderCard.Kind.NOTE) }
        val files = tree.attachments.keys.filter { it.parent == folder }.map { path ->
            val isImage = AttachmentKind.of(name = path.name) == AttachmentKind.IMAGE
            FolderCard(path = path, kind = if (isImage) FolderCard.Kind.IMAGE else FolderCard.Kind.FILE)
        }
        val entries = (notes + files).sortedWith(
            comparator = compareBy(comparator = naturalNameComparator) { card ->
                if (card.kind == FolderCard.Kind.NOTE) card.path.nameWithoutExtension else card.path.name
            }
        )
        return folders + entries
    }
}
