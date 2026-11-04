package app.notekea.core.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the system asks for less motion, as [app.notekea.core.platform.domain.SystemMotion] reads it, provided around
 * the app's windows; false when it cannot tell.
 */
val LocalSystemReduceMotion = staticCompositionLocalOf { false }

/**
 * Whether what moves on screen changes at once instead (`ROADMAP.md`, Motion): the setting in Settings → Appearance or
 * the system's, set by [NotekeaTheme]. Animations take their specs from [motionTween].
 */
val LocalReduceMotion = staticCompositionLocalOf { false }
