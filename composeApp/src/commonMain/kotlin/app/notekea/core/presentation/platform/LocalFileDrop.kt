package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Makes an element accept files dropped from the system's file manager: given what to do with the dropped files' paths,
 * it returns the modifier that listens for them. The desktop window provides it; elsewhere nothing can be dropped.
 */
val LocalFileDrop = staticCompositionLocalOf<(onDrop: (List<String>) -> Unit) -> Modifier> { { Modifier } }
