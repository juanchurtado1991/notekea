package app.notekea.core.presentation.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf

/** The [NoteLooks] of the open vault, provided around its workspace; no colors and no daily notes outside one. */
val LocalNoteLooks = staticCompositionLocalOf { NoteLooks() }
