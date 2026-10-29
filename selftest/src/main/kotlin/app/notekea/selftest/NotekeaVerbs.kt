package app.notekea.selftest

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import com.testbuddy.isShown
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.Shortcut
import com.testbuddy.ShortcutModifier.Mod
import java.nio.file.Files

private const val OPEN_FOLDER = "Open folder…"
private const val FIND_NOTE = "Find a note by name or alias"
private const val WAIT_MILLIS = 10_000L
private const val RELAUNCH_MILLIS = 30_000L

/** The Notekea a case drives, to set what its pickers answer. */
fun CaseScope.notekea(): CaseApp = CaseApps.of(vaults = folders.data)

/**
 * Quits Notekea as a person closing its window (the open note is saved first), runs [whileClosed] (another app
 * changing files meanwhile) and launches it again on the same data, waiting until the new one is up.
 */
fun CaseScope.relaunch(whileClosed: () -> Unit = {}) {
    val quitting = CaseApps.quit(vaults = folders.data, whileClosed = whileClosed)
    // Quitting waits for the window's close guard, which gives each of its handlers a few seconds.
    ui.waitUntil(timeoutMillis = RELAUNCH_MILLIS) {
        runCatching { CaseApps.of(vaults = folders.data) !== quitting }.getOrDefault(false)
    }
    ui.waitForIdle()
}

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
    pressInWindow(shortcut = Mod + Key.P)
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

/** Waits until the folder at [path] of the case's data exists. */
fun CaseScope.expectFolder(path: String) {
    val folder = file(path = path)
    try {
        ui.waitUntil(timeoutMillis = WAIT_MILLIS) { Files.isDirectory(folder) }
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected the folder \"$path\", it does not exist").apply { initCause(exception) }
    }
}

/** The row of the sidebar's tree that shows [text]: of every element showing it, the one furthest left. */
fun CaseScope.treeRow(text: String): SemanticsNodeInteraction {
    expectText(text = text)
    val nodes = ui.onAllNodes(matcher = hasText(text = text))
    val count = nodes.fetchSemanticsNodes().size
    val leftmost = (0 until count).minBy { nodes[it].fetchSemanticsNode().boundsInRoot.left }
    return nodes[leftmost]
}

/** Clicks the row of the tree that shows [text]. */
fun CaseScope.clickInTree(text: String) {
    treeRow(text = text).performClick()
}

/** Right-clicks the row of the tree that shows [text], for its menu. */
fun CaseScope.rightClickInTree(text: String) {
    treeRow(text = text).performMouseInput { rightClick() }
}

/** Renames the tree's [name] to [newName] from its menu, as a person would, and confirms with Enter. */
fun CaseScope.renameInTree(
    name: String,
    newName: String
) {
    rightClickInTree(text = name)
    click(text = "Rename")
    type(text = newName)
    press(key = Key.Enter)
}

/** The note's text field, found by a part of its text. */
fun CaseScope.noteText(part: String): SemanticsNodeInteraction {
    expectText(text = part, substring = true)
    return ui.onNode(matcher = hasSetTextAction() and hasText(text = part, substring = true))
}

/** Types [text] at the end of the open note whose text holds [part]. */
fun CaseScope.typeAtEnd(
    part: String,
    text: String
) {
    val field = noteText(part = part)
    field.performClick()
    val length = field.fetchSemanticsNode().config[SemanticsProperties.EditableText].text.length
    field.performTextInputSelection(selection = TextRange(index = length))
    field.performTextInput(text = text)
}

/** The open note's title field, showing [name]. */
fun CaseScope.titleField(name: String): SemanticsNodeInteraction {
    expectText(text = name)
    return ui.onNode(matcher = hasSetTextAction() and hasText(text = name))
}

/**
 * Presses [shortcut] as the window gets it: in the app every key reaches the window, in a case only a focused element
 * does, so when nothing has the keyboard (a note in Preview) the window takes it first.
 */
fun CaseScope.pressInWindow(shortcut: Shortcut) {
    val hasFocus = ui.onAllNodes(matcher = isFocused()).fetchSemanticsNodes().isNotEmpty()
    if (!hasFocus) ui.runOnIdle { notekea().windowFocus.requestFocus() }
    press(shortcut = shortcut)
}

/** Fails unless [texts] show on screen from top to bottom in that order (the first of each that is shown). */
fun CaseScope.expectOrderOnScreen(texts: List<String>) {
    texts.forEach { expectText(text = it) }
    val tops = texts.map { text ->
        val nodes = ui.onAllNodes(matcher = hasText(text = text))
        val count = nodes.fetchSemanticsNodes().size
        val shown = (0 until count).map { nodes[it] }.firstOrNull { it.isShown() } ?: nodes[0]
        shown.fetchSemanticsNode().boundsInRoot.top
    }
    if (tops != tops.sorted()) throw CaseFailure(message = "expected ${texts.joinToString()} in that order")
}
