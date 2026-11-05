package app.notekea.core.presentation.theme.palette

import androidx.compose.ui.graphics.Color

/**
 * One mode of a theme (`ROADMAP.md` 2.12), the way Workspace Blue works: quiet grays with a touch of the theme's hue, so
 * the [page], the [surface] of the content and the [raised] parts stay calm, and one [lead] color for what can be clicked.
 * [selection] tints a chosen row and [accent] marks tags and warnings. All chosen by hand, as tones computed from a brand
 * color painted the whole window with it.
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
