package app.notekea.editor.data

/**
 * The bit rates that fit a video of [seconds] in [maxBytes] (a little under, for the container), audio first, and
 * the height to scale it to when that rate is too low for its full size.
 */
internal data class VideoBitrate(
    val videoKbps: Long,
    val audioKbps: Long,
    val maxHeight: Int?
) {
    companion object {
        private const val BITS_PER_BYTE = 8
        private const val KILO = 1000
        private const val SHARE = 0.95
        private const val AUDIO_KBPS = 128L
        private const val LOW_AUDIO_KBPS = 64L
        private const val LOW_TOTAL_KBPS = 800L
        private const val HD_KBPS = 1500L
        private const val SD_KBPS = 700L
        private const val HD_HEIGHT = 720
        private const val SD_HEIGHT = 480
        private const val MIN_VIDEO_KBPS = 100L

        fun of(
            seconds: Double,
            maxBytes: Long
        ): VideoBitrate {
            val length = seconds.coerceAtLeast(minimumValue = 1.0)
            val totalKbps = (maxBytes * BITS_PER_BYTE * SHARE / length / KILO).toLong()

            val audio = if (totalKbps < LOW_TOTAL_KBPS) LOW_AUDIO_KBPS else AUDIO_KBPS
            val video = (totalKbps - audio).coerceAtLeast(minimumValue = MIN_VIDEO_KBPS)

            val height = when {
                video < SD_KBPS -> SD_HEIGHT
                video < HD_KBPS -> HD_HEIGHT
                else -> null
            }

            return VideoBitrate(
                videoKbps = video,
                audioKbps = audio,
                maxHeight = height
            )
        }
    }
}
