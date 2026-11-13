package app.notekea.core.presentation.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** The name of an entry that is gone: as readable as any muted text, so what says it is gone is not its color. */
val ColorScheme.missingEntry: Color
    get() = onSurfaceVariant

/**
 * The sidebar's material (macOS redesign 4A): the tool rail, the vault's tree and the side panel, in the theme's second
 * color, so the note stands in front. Kept in [ColorScheme.surfaceDim], which no Material component here reads.
 */
val ColorScheme.sidebarSurface: Color
    get() = surfaceDim

/**
 * The window's chrome (4A): the row of tabs, between the sidebar and the note, in the theme's second color. Kept in
 * [ColorScheme.surfaceBright].
 */
val ColorScheme.chromeSurface: Color
    get() = surfaceBright
