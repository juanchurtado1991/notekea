package app.notekea.core.presentation.designsystem.components.attachments

/** The words of [NotekeaVideoControls]' buttons and bars, for screen readers and tooltips. */
data class VideoFrameLabels(
    val play: String,
    val pause: String,
    val seek: String,
    val mute: String,
    val unmute: String,
    val volume: String,
    val fullscreen: String,
    val exitFullscreen: String
)
