package app.notekea.clips.domain

/** Where a section's clip stands. */
sealed interface SectionStep {
    /** Page [done] + 1 of [total] is being clipped. */
    data class Clipping(
        val done: Int,
        val total: Int,
        val url: String
    ) : SectionStep

    data class Done(
        val main: WebClip,
        val pages: List<WebClip>,
        val skipped: List<String>
    ) : SectionStep

    /** The page itself could not be clipped; nothing is written. */
    data class Failed(val error: ClipError) : SectionStep
}
