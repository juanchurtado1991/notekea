package app.notekea.notes.presentation.tree

/** The question asked after a rename when other notes link to the renamed entry. */
data class LinkUpdatePrompt(
    val oldName: String,
    val newName: String,
    val noteCount: Int
)
