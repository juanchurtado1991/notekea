package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The title bar the content runs under (macOS, redesign 1A); null where the system draws its own above the content. */
val LocalTitleBarSpace = staticCompositionLocalOf<TitleBarSpace?> { null }
