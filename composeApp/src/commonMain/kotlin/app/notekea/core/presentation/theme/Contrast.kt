package app.notekea.core.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** WCAG contrast between two colors, from 1 (the same) to 21 (black on white). */
internal fun contrastOf(
    first: Color,
    second: Color
): Float {
    val lighter = maxOf(first.luminance(), second.luminance())
    val darker = minOf(first.luminance(), second.luminance())
    return (lighter + LUMINANCE_FLARE) / (darker + LUMINANCE_FLARE)
}

/**
 * [color], moved toward black or white (whichever reads better on [backgrounds]) only as far as it needs to reach
 * [ratio] on all of them, so a theme's tone changes as little as possible.
 */
internal fun readableOn(
    color: Color,
    backgrounds: List<Color>,
    ratio: Float = WCAG_AA
): Color {
    val pole = if (backgrounds.all {
            contrastOf(Color.Black, it) >= contrastOf(Color.White, it)
        }
    ) {
        Color.Black
    } else {
        Color.White
    }
    var step = 0
    var shown = color
    while (backgrounds.any { contrastOf(shown, it) < ratio } && step < STEPS) {
        step++
        shown = lerp(start = color, stop = pole, fraction = step / STEPS.toFloat())
    }
    return shown
}

/** WCAG AA for body text. */
internal const val WCAG_AA = 4.5f

private const val LUMINANCE_FLARE = 0.05f

// Steps of 2 %: fine enough that a tone is never pushed visibly further than it needs.
private const val STEPS = 50
