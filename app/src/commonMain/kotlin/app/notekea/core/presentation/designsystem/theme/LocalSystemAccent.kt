package app.notekea.core.presentation.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The system's accent color (macOS and Windows), or null where there is none; used when Appearance asks for it. */
val LocalSystemAccent = staticCompositionLocalOf<Color?> { null }
