package app.notekea.clips.domain

/**
 * A page clipped into a note: its [title], the [notes] the model wrote from it (empty without a model, or when no
 * sentence was backed by the article), the YouTube [videos] it plays, and the [article], kept under "Original text".
 */
data class WebClip(
    val url: String,
    val title: String,
    val notes: String,
    val article: Article,
    val videos: List<PageVideo> = emptyList()
)
