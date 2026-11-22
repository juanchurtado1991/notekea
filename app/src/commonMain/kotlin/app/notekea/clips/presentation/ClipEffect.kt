package app.notekea.clips.presentation

import app.notekea.core.notes.domain.RelativePath

sealed interface ClipEffect {
    /** The clip was written at [path]: the window opens it. */
    data class Opened(val path: RelativePath) : ClipEffect

    /** An update rewrote the notes of [originals], each from that text: an editor that has one open reads it again. */
    data class Rewritten(val originals: Map<RelativePath, String>) : ClipEffect
}
