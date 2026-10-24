package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath

/** A rename in progress: the entry, the name being typed and why the last attempt was refused, if it was. */
data class RenameDraft(
    val path: RelativePath,
    val text: String,
    val error: NotesError? = null
)
