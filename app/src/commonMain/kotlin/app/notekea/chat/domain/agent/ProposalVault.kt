package app.notekea.chat.domain.agent

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/** The vault a proposal is checked against: its [path], its [notes] and the note open in the window. */
class ProposalVault(
    val path: String,
    val notes: Collection<NoteEntry>,
    val openNote: RelativePath?
)
