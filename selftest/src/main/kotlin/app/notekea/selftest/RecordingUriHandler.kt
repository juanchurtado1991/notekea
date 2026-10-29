package app.notekea.selftest

import androidx.compose.ui.platform.UriHandler

/** Opening a web link, in a case: it records the address instead of opening the browser. */
class RecordingUriHandler : UriHandler {
    val opened = mutableListOf<String>()

    override fun openUri(uri: String) {
        opened += uri
    }
}
