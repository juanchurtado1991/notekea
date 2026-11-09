package app.notekea.core.presentation.designsystem.theme.palette

import androidx.compose.ui.graphics.Color

/**
 * One mode of a theme: quiet grays with a touch of the theme's hue for the [page], the [surface] of the content and the
 * [raised] parts, and one [lead] color for what can be clicked. [selection] tints a chosen row and [accent] marks tags
 * and warnings. All are chosen by hand: tones computed from a brand color paint the whole window with it.
 */
internal data class ThemeColors(
    val page: Color,
    val surface: Color,
    val raised: Color,
    val ink: Color,
    val muted: Color,
    val lead: Color,
    val onLead: Color,
    val selection: Color,
    val outline: Color,
    val accent: Color
)
