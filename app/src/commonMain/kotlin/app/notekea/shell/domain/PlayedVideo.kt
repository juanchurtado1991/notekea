package app.notekea.shell.domain

/** The YouTube video the YouTube tab plays: its [id] and the [title] its note gives it, the tab's name. */
data class PlayedVideo(
    val id: String,
    val title: String
)
