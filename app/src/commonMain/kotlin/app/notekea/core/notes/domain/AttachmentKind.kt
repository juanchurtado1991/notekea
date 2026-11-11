package app.notekea.core.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * What an embed shows: an image Notekea can draw, a video it plays in place, any other file, shown as a chip that opens
 * with the system's app (PDF, audio, SVG), or a note shown in place. [of] tells a file's kind from its extension.
 */
enum class AttachmentKind {
    IMAGE,
    VIDEO,
    OTHER,

    /** A note embedded in another (`![[Other note]]`), whose text is shown in place. */
    NOTE;

    companion object {
        private const val EXTENSION_MARK = "."
        private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp")
        private val VIDEO_EXTENSIONS = setOf("mp4", "m4v", "mov", "webm", "mkv", "ogv")

        fun of(name: String): AttachmentKind {
            val extension = name.substringAfterLast(
                delimiter = EXTENSION_MARK,
                missingDelimiterValue = StringPatterns.EMPTY
            )

            return when (extension.lowercase()) {
                in IMAGE_EXTENSIONS -> IMAGE
                in VIDEO_EXTENSIONS -> VIDEO
                else -> OTHER
            }
        }
    }
}
