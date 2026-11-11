package app.notekea.core.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Plays the video file at an absolute path in a `NotekeaVideoFrame`, whose ✕ calls `onClose`; `onCannotPlay` when the
 * system cannot play it. The desktop gives it; where there is none a video stays a card that opens with its app.
 */
val LocalVideoPlayer = staticCompositionLocalOf<
    (
        @Composable (
            location: String,
            onClose: () -> Unit,
            onCannotPlay: () -> Unit,
            modifier: Modifier
        ) -> Unit
    )?
    > { null }
