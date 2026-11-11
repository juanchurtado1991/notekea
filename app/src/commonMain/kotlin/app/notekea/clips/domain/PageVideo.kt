package app.notekea.clips.domain

/** A YouTube video a clipped page plays: its [id] and the [title] the page gives it, when it gives one. */
data class PageVideo(
    val id: String,
    val title: String?
)
