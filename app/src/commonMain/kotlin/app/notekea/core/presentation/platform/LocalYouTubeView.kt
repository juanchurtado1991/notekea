package app.notekea.core.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Plays the YouTube video of an id inside Notekea's window, over the room its `modifier` gives (the YouTube tab);
 * `isCovered` while a dialog of the window is over it, which it then lets show. Where there is none (null), a video
 * plays as [LocalYouTubePlayer] says.
 */
val LocalYouTubeView = staticCompositionLocalOf<
    (
        @Composable (
            id: String,
            isCovered: Boolean,
            modifier: Modifier
        ) -> Unit
    )?
    > { null }
