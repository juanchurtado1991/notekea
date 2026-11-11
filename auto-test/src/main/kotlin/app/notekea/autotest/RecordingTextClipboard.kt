package app.notekea.autotest

import app.notekea.core.logging.domain.TextClipboard

/** The clipboard, in a case: it keeps what is copied, and how many times, instead of the system's clipboard. */
class RecordingTextClipboard : TextClipboard {
    @Volatile
    var copied: String? = null
        private set

    @Volatile
    var copies: Int = 0
        private set

    override fun copy(text: String) {
        copied = text
        copies++
    }
}
