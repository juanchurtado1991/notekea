package app.notekea.core.presentation.platform

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Which [WindowDialog] the window shows now, if any; [WindowDialogsHost] draws it. */
@Stable
class WindowDialogs {
    var shown by mutableStateOf<WindowDialog?>(value = null)
}
