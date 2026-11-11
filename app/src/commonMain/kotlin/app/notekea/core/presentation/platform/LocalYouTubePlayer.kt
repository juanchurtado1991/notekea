package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Plays the YouTube video of an id, with its title, in a window of Notekea's own (the system's web view) once the
 * person clicks play; where there is none, the video opens in the browser.
 */
val LocalYouTubePlayer = staticCompositionLocalOf<((id: String, title: String) -> Unit)?> { null }
