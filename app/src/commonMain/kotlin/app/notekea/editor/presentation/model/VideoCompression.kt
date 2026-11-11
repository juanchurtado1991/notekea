package app.notekea.editor.presentation.model

/**
 * A video over GitHub's limit being made smaller before it is attached: its [name], whether FFmpeg is still being
 * downloaded ([isDownloading]) and how far that step is ([fraction], null until it starts).
 */
data class VideoCompression(
    val name: String,
    val isDownloading: Boolean = false,
    val fraction: Float? = null
)
