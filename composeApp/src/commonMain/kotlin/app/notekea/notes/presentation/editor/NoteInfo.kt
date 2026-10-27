package app.notekea.notes.presentation.editor

import app.notekea.core.domain.time.LocalMoment
import app.notekea.notes.domain.NoteStats

/**
 * The side panel's Note info for the open note: what it holds ([stats], null until first counted) and when its file
 * was created and last modified, where the user is (null where the file system does not say).
 */
data class NoteInfo(
    val stats: NoteStats? = null,
    val createdAt: LocalMoment? = null,
    val modifiedAt: LocalMoment? = null
)
