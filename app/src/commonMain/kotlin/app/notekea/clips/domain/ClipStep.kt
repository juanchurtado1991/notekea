package app.notekea.clips.domain

/** Where a clip stands, as the clip dialog and the chat show it. */
sealed interface ClipStep {
    data object Fetching : ClipStep

    /** The article was found; the model writes notes about it. */
    data class Writing(val title: String) : ClipStep

    /** The notes are checked against the article. */
    data object Checking : ClipStep

    data class Done(val clip: WebClip) : ClipStep

    data class Failed(val error: ClipError) : ClipStep
}
