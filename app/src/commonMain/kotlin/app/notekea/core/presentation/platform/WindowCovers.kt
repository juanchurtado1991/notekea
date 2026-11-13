package app.notekea.core.presentation.platform

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * How many things are drawn over the window now (dialogs, menus, popovers, the palette), each telling with
 * [CoversWindow]: a native view in the window (the YouTube player) hides while any is, since it would cover them.
 */
@Stable
class WindowCovers {
    var count by mutableIntStateOf(value = 0)

    val isCovered: Boolean
        get() = count > 0
}
