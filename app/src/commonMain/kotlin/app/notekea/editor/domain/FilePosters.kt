package app.notekea.editor.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * A small picture of a file that is not one, for its card in a note (the developer's request of 2026-11-13): a PDF's
 * first page, and a video's first frame that is not black. Null when the file has none or it cannot be made.
 */
fun interface FilePosters {
    suspend fun of(
        vaultPath: String,
        path: RelativePath
    ): ByteArray?

    companion object {
        /** Whether [path] is a file a poster is made of. */
        fun has(path: RelativePath): Boolean =
            FilesShownHere.isVideo(path = path) || FilesShownHere.isPdf(path = path)
    }
}
