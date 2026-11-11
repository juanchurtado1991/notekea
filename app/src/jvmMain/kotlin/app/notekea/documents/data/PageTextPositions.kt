package app.notekea.documents.data

import app.notekea.documents.domain.PageMark
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition

/**
 * Where a passage lies on a PDF page: the page's text with the position of each letter, the passage looked for by its
 * first words (spaces and case set aside, as Tika and PDFBox space words differently), and a mark per line it covers.
 */
internal object PageTextPositions {
    private const val LOOKED_FOR = 80
    private val SPACES = Regex(pattern = "\\s+")

    fun marksOf(
        document: PDDocument,
        page: Int,
        passage: String
    ): List<PageMark> {
        val letters = mutableListOf<TextPosition>()

        val stripper = object : PDFTextStripper() {
            override fun writeString(
                text: String?,
                textPositions: MutableList<TextPosition>?
            ) {
                letters += textPositions.orEmpty()
            }
        }

        stripper.startPage = page + 1
        stripper.endPage = page + 1
        stripper.getText(document)

        val kept = letters.filter { it.unicode.isNotBlank() }
        val pageText = kept.joinToString(separator = "") { it.unicode }.lowercase()

        val wanted = passage
            .replace(SPACES, "")
            .lowercase()
            .take(n = LOOKED_FOR)

        if (wanted.isEmpty()) return emptyList()

        val start = pageText.indexOf(string = wanted)
        if (start < 0) return emptyList()

        val found = positionsCovering(
            kept = kept,
            start = start,
            length = wanted.length
        )

        val box = document.getPage(page).mediaBox

        return found
            .groupBy { (it.yDirAdj / it.heightDir).toInt() }
            .values
            .map { line ->
                val left = line.minOf { it.xDirAdj }

                val right = line.maxOf {
                    it.xDirAdj + it.widthDirAdj
                }

                val top = line.minOf {
                    it.yDirAdj - it.heightDir
                }

                val bottom = line.maxOf { it.yDirAdj }

                PageMark(
                    left = left / box.width,
                    top = top / box.height,
                    width = (right - left) / box.width,
                    height = (bottom - top) / box.height
                )
            }
    }

    // The letters whose characters make the [length] characters from [start] of the page's text.
    private fun positionsCovering(
        kept: List<TextPosition>,
        start: Int,
        length: Int
    ): List<TextPosition> {
        var offset = 0

        return kept.filter { letter ->
            val at = offset
            offset += letter.unicode.length

            at + letter.unicode.length > start && at < start + length
        }
    }
}
