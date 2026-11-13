package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.cursorToNoteStart
import app.notekea.autotest.notekea
import app.notekea.autotest.pressInWindow
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val IMAGES_NOTE = "Images and files"
private const val IMAGES_FILE = "$PLAYGROUND/Editor/Images and files.md"
private const val IMAGES_SHOWN = "A picture at its own size"
private const val MENU_PDF = "Weekly menu.pdf"

// The PDF's tab says it, the note never does.
private const val OPEN_WITH_APP = "Open in the system’s app"

/**
 * The cases of 1.8 that need the system's dialogs and apps, checked through the case's own picker and opener (the
 * developer's choice of 2026-11-11: automate every case a text, an order or a count can check).
 */
internal fun SuiteBuilder.attachmentSystemCases() {
    pdfOpens()
    locateMissing()
    attachFromMenu()
}

// The developer's smoke of 2026-11-13: a PDF opens in a tab of Notekea, as a double click in Files opens it.
private fun SuiteBuilder.pdfOpens() {
    automated(
        id = "1.8-03",
        title = "A PDF's card and a link to it open the PDF in a tab of Notekea, not with the system's app"
    ) {
        openPlaygroundNote(
            name = IMAGES_NOTE,
            shownText = IMAGES_SHOWN
        )

        cursorToNoteStart()
        scrollTo(text = MENU_PDF)
        expectText(text = "Opens in a tab")
        click(text = MENU_PDF)
        expectText(text = OPEN_WITH_APP)
        pressInWindow(shortcut = Mod + Key.W)
        expectNoText(text = OPEN_WITH_APP)
        clickWords(words = "the menu")
        expectText(text = OPEN_WITH_APP)

        if (notekea().fileOpener.opened.isNotEmpty()) {
            throw CaseFailure(message = "expected $MENU_PDF opened in a tab, it opened with its app")
        }
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
        scrollToEmbedded(text = "Locate…")
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
