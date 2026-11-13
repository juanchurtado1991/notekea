package app.notekea.shell.presentation.follow

import androidx.compose.runtime.staticCompositionLocalOf
import app.notekea.clips.presentation.ClipViewModel

/** The open vault's clips, for the status bar's entry of an update of its web sources, drawn far from them. */
internal val LocalVaultClips = staticCompositionLocalOf<ClipViewModel?> { null }
