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
}
