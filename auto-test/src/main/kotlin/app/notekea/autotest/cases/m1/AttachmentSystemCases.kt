package app.notekea.autotest.cases.m1

import app.notekea.autotest.cursorToNoteStart
import app.notekea.autotest.notekea
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.SuiteBuilder

private const val IMAGES_NOTE = "Images and files"
private const val IMAGES_FILE = "$PLAYGROUND/Editor/Images and files.md"
private const val IMAGES_SHOWN = "A picture at its own size"
private const val MENU_PDF = "Weekly menu.pdf"
private const val WAIT_MILLIS = 10_000L

/**
 * The cases of 1.8 that need the system's dialogs and apps, checked through the case's own picker and opener (the
 * developer's choice of 2026-11-11: automate every case a text, an order or a count can check).
 */
internal fun SuiteBuilder.attachmentSystemCases() {
    pdfOpens()
    locateMissing()
    attachFromMenu()
}

private fun SuiteBuilder.pdfOpens() {
    automated(
        id = "1.8-03",
        title = "A PDF's chip and a link to it open the PDF with the system's app"
    ) {
        openPlaygroundNote(
            name = IMAGES_NOTE,
            shownText = IMAGES_SHOWN
        )

        cursorToNoteStart()
        scrollTo(text = MENU_PDF)
        click(text = MENU_PDF)
        awaitOpened(times = 1)
        clickWords(words = "the menu")
        awaitOpened(times = 2)
    }
}

private fun SuiteBuilder.locateMissing() {
    automated(
        id = "1.8-04",
        title = "Locate… points a missing picture at the file chosen, copied into the attachments folder"
    ) {
        openPlaygroundNote(
            name = IMAGES_NOTE,
            shownText = IMAGES_SHOWN
        )

        notekea().attachmentPicker.nextPick = outside(name = "Found plate.png")
        cursorToNoteStart()
        scrollTo(text = "Locate…")
        click(text = "Locate…")

        expectFile(
            path = IMAGES_FILE,
            containing = "![[Found plate.png]]"
        )

        expectFile(path = "$PLAYGROUND/attachments/Found plate.png")
    }
}

private fun SuiteBuilder.attachFromMenu() {
    automated(
        id = "1.8-05",
        title = "More › Image or file attaches the file chosen and embeds it on a line of its own"
    ) {
        openPlaygroundNote(
            name = IMAGES_NOTE,
            shownText = IMAGES_SHOWN
        )

        notekea().attachmentPicker.nextPick = outside(name = "Shopping list.pdf")
        cursorToNoteStart()
        clickIcon(label = "More")
        click(text = "Image or file")

        expectFile(
            path = IMAGES_FILE,
            containing = "![[Shopping list.pdf]]"
        )

        expectFile(path = "$PLAYGROUND/attachments/Shopping list.pdf")
    }
}

/** The file [name] of the test data's Outside files folder, outside every vault. */
private fun CaseScope.outside(name: String): String = file(path = "Outside files/$name").toString()

/** Waits until the case's file opener was asked [times] times for the weekly menu's PDF. */
private fun CaseScope.awaitOpened(times: Int) {
    step(description = "Expect $MENU_PDF opened $times time(s) with its app")

    try {
        ui
            .waitUntil(timeoutMillis = WAIT_MILLIS) {
                notekea()
                    .fileOpener
                    .opened
                    .count {
                        it.name == MENU_PDF
                    } >= times
            }
    } catch (exception: androidx.compose.ui.test.ComposeTimeoutException) {
        throw CaseFailure(message = "expected $MENU_PDF opened $times time(s)").apply {
            initCause(exception)
        }
    }
}
