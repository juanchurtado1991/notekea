package app.notekea.core.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Plays the video file at an absolute path over the room its `modifier` gives, with its controls (volume, full
 * screen); `onCannotPlay` when the system cannot play it. The desktop gives it; where there is none a video opens
 * with its app.
 */
val LocalVideoPlayer = staticCompositionLocalOf<
    (
        @Composable (
            location: String,
            onCannotPlay: () -> Unit,
            modifier: Modifier
        ) -> Unit
    )?
    > { null }
