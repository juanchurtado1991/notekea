package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * What an embed shows: an image Notekea can draw, any other file, shown as a chip that opens with the system's app
 * (PDF, audio, and image formats it cannot draw, such as SVG), or a note shown in place. [of] tells a file's kind from
 * its extension.
 */
enum class AttachmentKind {
    IMAGE,
    OTHER,

    /** A note embedded in another (`![[Other note]]`), whose text is shown in place. */
    NOTE;

    companion object {
        private const val EXTENSION_MARK = "."
        private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp")

        fun of(
            name: String
        ): AttachmentKind {
            val extension = name.substringAfterLast(
                delimiter = EXTENSION_MARK,
                missingDelimiterValue = StringPatterns.EMPTY
            )
            return if (extension.lowercase() in IMAGE_EXTENSIONS) IMAGE else OTHER
        }
    }
}
