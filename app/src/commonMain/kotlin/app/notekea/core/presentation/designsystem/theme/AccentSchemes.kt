package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/*
 * A theme with the system's accent color in place of its own (macOS redesign 4B). The accent is moved toward the
 * ink until links on the page, the note and the sidebar reach WCAG AA, and the text on it is white or black, whichever
 * reads better, so no accent a person picks makes a text unreadable.
 */

private const val CONTAINER_DARK = 0.28f
private const val CONTAINER_LIGHT = 0.14f
private const val LUMINANCE_OFFSET = 0.05f
private const val STEP = 0.08f
private const val STEPS = 12
private const val WCAG_AA = 4.5f

internal fun ColorScheme.withAccent(
    accent: Color,
    darkTheme: Boolean
): ColorScheme {
    val lead = readableLead(accent = accent)
    val onLead = listOf(Color.White, Color.Black).maxBy { contrastOf(it, lead) }

    return copy(
        primary = lead,
        onPrimary = onLead,
        primaryContainer = lerp(
            start = surface,
            stop = lead,
            fraction = if (darkTheme) CONTAINER_DARK else CONTAINER_LIGHT
        ),
        onPrimaryContainer = onSurface
    )
}

private fun ColorScheme.readableLead(accent: Color): Color {
    val grounds = listOf(background, surface, sidebarSurface)
    var lead = accent
    var step = 0

    fun isUnreadable() = grounds.any { ground ->
        contrastOf(lead, ground) < WCAG_AA
    }

    while (step < STEPS && isUnreadable()) {
        lead = lerp(
            start = lead,
            stop = onSurface,
            fraction = STEP
        )

        step++
    }

    return lead
}

internal fun contrastOf(
    first: Color,
    second: Color
): Float {
    val lighter = maxOf(first.luminance(), second.luminance())
    val darker = minOf(first.luminance(), second.luminance())

    return (lighter + LUMINANCE_OFFSET) / (darker + LUMINANCE_OFFSET)
}
