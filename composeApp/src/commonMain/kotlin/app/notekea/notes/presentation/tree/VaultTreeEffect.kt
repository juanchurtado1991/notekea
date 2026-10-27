package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.RelativePath

/** One-shot events of the vault sidebar for the panes around it. */
sealed interface VaultTreeEffect {
    /**
     * [from] is now [to] (a note, or a folder with everything in it), renamed in the tree; the editor follows the note
     * it has open instead of saving to a path that no longer exists.
     */
    data class EntryRenamed(
        val from: RelativePath,
        val to: RelativePath
    ) : VaultTreeEffect

    /** [path] was moved to the trash from the tree: its tabs close. */
    data class EntryDeleted(val path: RelativePath) : VaultTreeEffect

    /**
     * [path] (a note, another file or a folder) was opened: clicked or chosen with Enter in the tree, reached by a link,
     * the quick switcher, Today or the day arrows, or just created. Its tab opens, behind the current one when
     * [inBackground].
     */
    data class OpenEntry(
        val path: RelativePath,
        val inBackground: Boolean = false
    ) : VaultTreeEffect

    /** "New note from template" was chosen: the template picker opens for a new note in [folder]. */
    data class ChooseTemplate(val folder: RelativePath) : VaultTreeEffect
}
