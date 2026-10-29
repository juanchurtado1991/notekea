package app.notekea.selftest

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod

private const val OPEN_FOLDER = "Open folder…"
private const val FIND_NOTE = "Find a note by name or alias"
private const val WAIT_MILLIS = 10_000L

/** The Notekea a case drives, to set what its pickers answer. */
fun CaseScope.notekea(): CaseApp = CaseApps.of(vaults = folders.data)

/** Opens the vault [name] of the case's data with Open folder…, the picker answering it, until [shownText] shows. */
fun CaseScope.openVault(
    name: String,
    shownText: String
) {
    notekea().folderPicker.nextPick = notekea().vaultPath(name = name)
    click(text = OPEN_FOLDER)
    expectText(text = shownText)
}

/** Opens the note [name] with the quick switcher (Mod+P) as a person who knows its name, until [shownText] shows. */
fun CaseScope.openNote(
    name: String,
    shownText: String
) {
    press(shortcut = Mod + Key.P)
    expectText(text = FIND_NOTE)
    type(text = name)
    waitForResult(name = name)
    press(key = Key.Enter)
    expectText(text = shownText, substring = true)
}

/** Waits until the quick switcher lists [name] as a result, not only in its search field. */
fun CaseScope.waitForResult(name: String) {
    try {
        ui.waitUntilAtLeastOneExists(
            matcher = hasText(text = name) and !hasSetTextAction(),
            timeoutMillis = WAIT_MILLIS
        )
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected the quick switcher to list \"$name\", it did not").apply {
            initCause(exception)
        }
    }
}
