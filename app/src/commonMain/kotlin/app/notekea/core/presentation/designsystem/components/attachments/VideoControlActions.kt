package app.notekea.core.presentation.designsystem.components.attachments

/** What [NotekeaVideoControls] asks of its player: play or pause, a place, a volume (0 to 1), mute, full screen. */
data class VideoControlActions(
    val onPlayToggle: () -> Unit,
    val onSeek: (fraction: Float) -> Unit,
    val onVolumeChange: (volume: Float) -> Unit,
    val onMuteToggle: () -> Unit,
    val onFullscreenToggle: () -> Unit
)
