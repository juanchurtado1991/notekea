package app.notekea.clips.presentation

import app.notekea.core.notes.domain.RelativePath

sealed interface ClipEffect {
    /** The clip was written at [path]: the window opens it. */
    data class Opened(val path: RelativePath) : ClipEffect
}
