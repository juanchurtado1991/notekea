package app.notekea.clips.domain

/** A web page as downloaded: its [html] and the [url] it ended at, after any redirect. */
data class FetchedPage(
    val url: String,
    val html: String
)
