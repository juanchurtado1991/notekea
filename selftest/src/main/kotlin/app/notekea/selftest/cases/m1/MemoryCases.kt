package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import app.notekea.selftest.clickInTree
import app.notekea.selftest.expectOrderOnScreen
import app.notekea.selftest.openNote
import app.notekea.selftest.pressInWindow
import app.notekea.selftest.typeAtEnd
import app.notekea.selftest.waitForResult
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val PLATE = "Arepas plate.png"
private const val IMAGES = "Images and files"
private val JOURNAL_DAYS = listOf("2026-10-20", "2026-10-21", "2026-10-22")

/** The cases of 1.18, what the editor lets go of and reads again (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.memoryCases() {
    savedOnSwitchingVault()
    sharpPictures()
    picturesComeBack()
    namesInOrder()
}

private fun SuiteBuilder.savedOnSwitchingVault() {
    automated(id = "1.18-01", title = "A word typed just before switching vaults is saved") {
        openPlaygroundNote(name = "Arepas", shownText = AREPAS_FLOUR)
        typeAtEnd(part = AREPAS_FLOUR, text = "\nLast word.")
        clickIcon(label = "Open another vault")
        expectText(text = "Open folder…")
        click(text = PLAYGROUND)
        expectText(text = WELCOME)
        openNote(name = "Arepas", shownText = "Last word.")
        expectFile(path = "$PLAYGROUND/Recipes/Arepas.md", containing = "Last word.")
    }
}

private fun SuiteBuilder.sharpPictures() {
    manual(id = "1.18-02", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "A folder's cards and an opened picture are sharp")
        steps(
            text =
            "1. In Notekea, open the Playground Vault from the Test data folder (Open folder…, after Open another vault at the bottom left if a vault is open).\n2. " +
                "Click the attachments folder in the list on the left and look at the card of Market photo.jpg.\n3. " +
                "Click that card."
        )
        expected(
            text = "The card shows the photo cropped to the card and sharp: \"Market photo 4000 x 3000\" reads at " +
                "its top. The photo opens in a tab and shows whole, sharp."
        )
    }
}

private fun SuiteBuilder.picturesComeBack() {
    automated(id = "1.18-03", title = "A note's pictures show again when its tab comes back") {
        openPlaygroundNote(name = IMAGES, shownText = "A picture at its own size")
        pressInWindow(shortcut = Mod + Key.E)
        expectPictures(atLeast = 2)
        openNote(name = "Arepas", shownText = AREPAS_FLOUR)
        tab(name = IMAGES).performClick()
        expectPictures(atLeast = 2)
    }
}

private fun SuiteBuilder.namesInOrder() {
    automated(id = "1.18-04", title = "Names with numbers are listed in order, in the quick switcher and the tree") {
        openPlaygroundNote(name = WELCOME, shownText = "This is a sample vault")
        pressInWindow(shortcut = Mod + Key.P)
        type(text = "2026-10")
        JOURNAL_DAYS.forEach { waitForResult(name = it) }
        expectOrderOnScreen(texts = JOURNAL_DAYS)
        press(key = Key.Escape)
        clickInTree(text = "Journal")
        expectOrderOnScreen(texts = JOURNAL_DAYS)
    }
}

private fun CaseScope.expectPictures(atLeast: Int) {
    val holds = runCatching {
        ui.waitUntil(timeoutMillis = 10_000) {
            ui.onAllNodes(matcher = hasContentDescription(value = PLATE)).fetchSemanticsNodes().size >= atLeast
        }
    }.isSuccess
    if (!holds) throw CaseFailure(message = "expected the pictures of $IMAGES to show")
}
