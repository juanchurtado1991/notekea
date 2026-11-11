package app.notekea.core.documents.data

import org.apache.pdfbox.pdmodel.font.FontInfo
import org.apache.pdfbox.pdmodel.font.FontMappers
import org.apache.pdfbox.pdmodel.font.FontProvider

/**
 * PDFBox without the system's fonts: a font a PDF does not embed is drawn with PDFBox's own (Liberation), instead of
 * PDFBox scanning every font of the computer on first use, which takes tens of seconds on Windows and macOS and writes
 * `.pdfbox.cache` in the user's home folder. Text is read the same; only the look of such fonts changes.
 */
object PdfFonts {
    private var isSet = false

    /** Before PDFBox looks a font up; once is enough, again does nothing. */
    @Synchronized
    fun useBundledOnly() {
        if (isSet) return

        // The mapper's class is not public, its setProvider is: set before any lookup, nothing is scanned.
        val mapper = FontMappers.instance()

        mapper
            .javaClass
            .getMethod(SET_PROVIDER, FontProvider::class.java)
            .apply { isAccessible = true }
            .invoke(mapper, NoSystemFonts)

        isSet = true
    }

    /** The fonts PDFBox may use from the computer: none. */
    internal object NoSystemFonts : FontProvider() {
        override fun toDebugString(): String = DEBUG

        override fun getFontInfo(): List<FontInfo> = emptyList()
    }

    private const val SET_PROVIDER = "setProvider"
    private const val DEBUG = "No system fonts (Notekea)"
}
