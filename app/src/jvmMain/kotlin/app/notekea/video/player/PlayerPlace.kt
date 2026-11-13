package app.notekea.video.player

/**
 * Where the YouTube player sits in the window's content, in points from its top left: what AppKit places a view by,
 * while Compose lays out in pixels ([of]).
 */
internal data class PlayerPlace(
    val left: Double,
    val top: Double,
    val width: Double,
    val height: Double
) {
    companion object {
        /**
         * The place of bounds in pixels on a screen that draws [scale] pixels a point (2 on a Retina screen, whatever
         * the app's own zoom is).
         */
        fun of(
            left: Float,
            top: Float,
            width: Float,
            height: Float,
            scale: Double
        ): PlayerPlace = PlayerPlace(
            left = left / scale,
            top = top / scale,
            width = width / scale,
            height = height / scale
        )

        /**
         * A view's y for AppKit: from the top when its parent [isFlipped], else from the bottom of a content
         * [contentHeight] high.
         */
        fun yOf(
            top: Double,
            height: Double,
            contentHeight: Double,
            isFlipped: Boolean
        ): Double = if (isFlipped) top else contentHeight - top - height
    }
}
