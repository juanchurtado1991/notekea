package app.notekea.core.documents.data

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File

/** Real documents written for the tests, each with one sentence to find: what a vault holds, made on the spot. */
internal object DocumentFixtures {
    const val SENTENCE = "Trams climb the hills of Alfama."

    /** A PDF of two pages, [SENTENCE] on the second; with [isBlank], two pages without text, as a scan. */
    fun pdf(
        file: File,
        isBlank: Boolean = false
    ) = PDDocument().use { document ->
        listOf("Lisbon, first page.", SENTENCE).forEach { line ->
            val page = PDPage()
            document.addPage(page)
            if (isBlank) return@forEach

            PDPageContentStream(document, page).use { content ->
                content.beginText()
                content.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 14f)
                content.newLineAtOffset(72f, 700f)
                content.showText(line)
                content.endText()
            }
        }

        document.save(file)
    }

    fun docx(file: File) = XWPFDocument().use { document ->
        document
            .createParagraph()
            .createRun()
            .setText("Lisbon guide")

        document
            .createParagraph()
            .createRun()
            .setText(SENTENCE)

        file.outputStream().use { document.write(it) }
    }

    fun xlsx(file: File) = XSSFWorkbook().use { workbook ->
        val sheet = workbook.createSheet("Budget")

        listOf(listOf("Item", "Cost"), listOf("Tram pass", "40")).forEachIndexed { index, cells ->
            val row = sheet.createRow(index)
            cells.forEachIndexed { column, value -> row.createCell(column).setCellValue(value) }
        }

        file.outputStream().use { workbook.write(it) }
    }

    fun pptx(file: File) = XMLSlideShow().use { show ->
        show
            .createSlide()
            .createTextBox()
            .setText(SENTENCE)

        file.outputStream().use { show.write(it) }
    }

    fun html(file: File) =
        file.writeText("<html><body><h1>Lisbon</h1><p>$SENTENCE</p></body></html>")

    /** An EPUB of one chapter, written by hand: the zip layout every e-book reader opens. */
    fun epub(file: File) = zip(
        file = file,
        entries = listOf(
            "mimetype" to "application/epub+zip",
            "META-INF/container.xml" to "<?xml version=\"1.0\"?><container version=\"1.0\" " +
                "xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\"><rootfiles><rootfile " +
                "full-path=\"content.opf\" media-type=\"application/oebps-package+xml\"/></rootfiles></container>",
            "content.opf" to "<?xml version=\"1.0\"?><package xmlns=\"http://www.idpf.org/2007/opf\" " +
                "version=\"3.0\"><metadata/><manifest><item id=\"c1\" href=\"chapter.xhtml\" " +
                "media-type=\"application/xhtml+xml\"/></manifest><spine><itemref idref=\"c1\"/></spine></package>",
            "chapter.xhtml" to "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body><h1>Lisbon</h1><p>$SENTENCE</p>" +
                "</body></html>"
        )
    )

    /** An OpenDocument text, written by hand: its mimetype first, then its content. */
    fun odt(file: File) = zip(
        file = file,
        entries = listOf(
            "mimetype" to "application/vnd.oasis.opendocument.text",
            "content.xml" to "<?xml version=\"1.0\"?><office:document-content " +
                "xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" " +
                "xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\"><office:body><office:text>" +
                "<text:p>$SENTENCE</text:p></office:text></office:body></office:document-content>",
            "META-INF/manifest.xml" to "<?xml version=\"1.0\"?><manifest:manifest " +
                "xmlns:manifest=\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\"><manifest:file-entry " +
                "manifest:full-path=\"/\" manifest:media-type=\"application/vnd.oasis.opendocument.text\"/>" +
                "<manifest:file-entry manifest:full-path=\"content.xml\" manifest:media-type=\"text/xml\"/>" +
                "</manifest:manifest>"
        )
    )

    private fun zip(
        file: File,
        entries: List<Pair<String, String>>
    ) = java
        .util
        .zip
        .ZipOutputStream(file.outputStream())
        .use { zip ->
            entries.forEach { (name, text) ->
                zip.putNextEntry(
                    java
                        .util
                        .zip
                        .ZipEntry(name)
                )

                zip.write(text.toByteArray())
                zip.closeEntry()
            }
        }
}
