package app.notekea.clips.presentation

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.ClipStep

/** The clip dialog: whether it [isOpen], the [url] typed, the [step] of the clip under way, and why it failed. */
data class ClipState(
    val isOpen: Boolean = false,
    val url: String = "",
    val step: ClipStep? = null,
    val error: ClipError? = null
) {
    val isBusy: Boolean get() =
        step != null &&
            step !is ClipStep.Done &&
            step !is ClipStep.Failed
    val canClip: Boolean get() = url.isNotBlank() && !isBusy
}
