package app.notekea.core.presentation.designsystem.theme.palette

import androidx.compose.ui.graphics.Color

/**
 * One mode of a theme, from its 60/30/10 palette: the first color makes the [page] and the [surface] of the content,
 * the second the [sidebar], the [chrome] (the row of tabs), the [raised] parts and the [selection], and the third the
 * [lead] color of what can be clicked and the [accent] of tags.
 */
internal data class ThemeColors(
    val page: Color,
    val surface: Color,
    val sidebar: Color,
    val chrome: Color,
    val raised: Color,
    val ink: Color,
    val muted: Color,
    val lead: Color,
    val onLead: Color,
    val selection: Color,
    val outline: Color,
    val accent: Color
)
