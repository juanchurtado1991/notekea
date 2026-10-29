package app.notekea.core.logging.data

import app.notekea.core.logging.domain.TextClipboard
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/** [TextClipboard] on the desktop's clipboard. */
class AwtTextClipboard : TextClipboard {
    override fun copy(
        text: String
    ) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }
}
