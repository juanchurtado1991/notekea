package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performTextClearance
import app.notekea.selftest.openVault
import app.notekea.selftest.waitForResult
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val PERSONAL_NOTES = "Personal Notes"
private const val WELCOME = "Welcome"

/** The cases of 1.6, the quick switcher and search (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.quickSwitcherCases() {
    automated(id = "1.6-01", title = "The quick switcher opens a note by part of its name") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        press(shortcut = Mod + Key.P)
        expectText(text = "Find a note by name or alias")
        type(text = "are")
        waitForResult(name = "Arepas")
        press(key = Key.Enter)
        expectText(text = "2 cups pre-cooked white corn flour", substring = true)
    }
    automated(id = "1.6-02", title = "The quick switcher finds aliases and names with accents") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        press(shortcut = Mod + Key.P)
        type(text = "corn cakes")
        waitForResult(name = "Arepas")
        expectText(text = "Corn cakes")
        ui.onNode(matcher = isFocused() and hasSetTextAction()).performTextClearance()
        type(text = "cafe")
        waitForResult(name = "Café con leche")
    }
    automated(id = "1.6-03", title = "The quick switcher matches a folder path") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        press(shortcut = Mod + Key.P)
        type(text = "gard/tom")
        waitForResult(name = "Tomato plan")
        expectText(text = "Projects/Garden/Tomato plan")
    }
}
