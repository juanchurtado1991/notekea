package app.notekea.selftest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** A case asking its Notekea to quit and start again, doing [whileClosed] in between. */
class QuitRequest {
    var isRequested by mutableStateOf(false)
        private set
    var whileClosed: () -> Unit = {}
        private set

    fun request(whileClosed: () -> Unit) {
        this.whileClosed = whileClosed
        isRequested = true
    }
}
