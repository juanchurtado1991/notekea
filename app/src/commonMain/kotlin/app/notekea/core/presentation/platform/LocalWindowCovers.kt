package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The window's [WindowCovers]: what is drawn over the window writes it, a native view in the window reads it. */
val LocalWindowCovers = staticCompositionLocalOf { WindowCovers() }
