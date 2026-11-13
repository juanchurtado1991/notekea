package app.notekea.editor.domain

import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.RelativePath

/**
 * The files a click in a note opens in a tab of Notekea, as a double click in Files does: the documents it reads (a
 * PDF with its pages) and the videos it plays. Any other file opens with its app (the developer's request, 2026-11-13).
 */
object FilesShownHere {
    fun holds(path: RelativePath): Boolean {
        val isVideo = AttachmentKind.of(name = path.name) == AttachmentKind.VIDEO

        return isVideo || DocumentFormat.of(path = path) != null
    }
}
