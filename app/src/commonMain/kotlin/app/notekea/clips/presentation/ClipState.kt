package app.notekea.clips.presentation

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.ClipStep
import app.notekea.clips.domain.SectionLinks
import app.notekea.clips.domain.SectionStep
import app.notekea.clips.domain.UpdateStep

/**
 * The clip dialog: whether it [isOpen], the [url] typed, the [step] of the clip under way, why it failed, and the
 * [section] it asks about before clipping it too, then the [progress] through its pages.
 */
data class ClipState(
    val isOpen: Boolean = false,
    val url: String = "",
    val step: ClipStep? = null,
    val error: ClipError? = null,
    // The pages the page links under its own path: the dialog asks whether to clip them too.
    val section: SectionLinks? = null,
    // What the section offers, in a sentence the model wrote; none without a model.
    val sectionSummary: String? = null,
    val progress: SectionStep.Clipping? = null,
    // An update of the web sources: how far it is, then what it changed; shown in a dialog of its own when asked for.
    val update: UpdateStep? = null,
    val isUpdateShown: Boolean = false,
    // How many days ago the web sources were last updated (0 is today), null when never: Settings → General says it.
    val daysSinceUpdate: Int? = null,
    // The last update read none of its pages (no connection): said where it was asked for without its dialog.
    val hasUpdateFailed: Boolean = false
) {
    val isAsking: Boolean get() =
        section != null &&
            step == null &&
            progress == null

    private val isStepRunning: Boolean get() =
        step != null &&
            step !is ClipStep.Done &&
            step !is ClipStep.Failed

    val isBusy: Boolean get() = progress != null || isStepRunning
    val canClip: Boolean get() = url.isNotBlank() && !isBusy
}
