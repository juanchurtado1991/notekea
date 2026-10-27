package app.notekea.core.appearance.domain

import kotlin.math.roundToInt

/**
 * How large the whole app is drawn (`ROADMAP.md` 1.14), as browsers and editors zoom: [DEFAULT] is the system's size,
 * and each step makes it [STEP] larger or smaller, between [MIN] and [MAX].
 */
object UiScale {
    const val DEFAULT = 1f
    const val MAX = 1.6f
    const val MIN = 0.8f
    private const val STEP = 0.1f
    private const val TENTHS = 10

    fun larger(
        scale: Float
    ): Float = clamp(scale = scale + STEP)

    fun smaller(
        scale: Float
    ): Float = clamp(scale = scale - STEP)

    /** [scale] kept between [MIN] and [MAX] and rounded to a tenth, so steps never drift. */
    fun clamp(
        scale: Float
    ): Float = ((scale * TENTHS).roundToInt() / TENTHS.toFloat()).coerceIn(minimumValue = MIN, maximumValue = MAX)
}
