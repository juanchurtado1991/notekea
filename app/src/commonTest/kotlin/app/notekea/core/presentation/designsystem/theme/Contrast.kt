package app.notekea.core.presentation.designsystem.theme

import androidx.compose.ui.graphics.Color
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

/** WCAG AA for body text. */
internal const val WCAG_AA = 4.5f

private const val LUMINANCE_FLARE = 0.05f
