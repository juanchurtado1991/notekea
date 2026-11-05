package app.notekea.selftest

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import app.notekea.core.presentation.TestTags
import com.testbuddy.isShown
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.Shortcut
import com.testbuddy.ShortcutModifier.Mod
import java.nio.file.Files

private const val INDEX_READY = "ready to search"
private const val OPEN_FOLDER = "Open folder…"
private const val FIND_NOTE = "Find a note by name or alias"
private const val WAIT_MILLIS = 10_000L
private const val RELAUNCH_MILLIS = 30_000L

// A busy machine (CI, several builds at once) indexes the Playground Vault in far more than a text's usual wait.
private const val INDEX_MILLIS = 60_000L

/** The Notekea a case drives, to set what its pickers answer. */
fun CaseScope.notekea(): CaseApp = CaseApps.of(vaults = folders.data)

/**
 * Quits Notekea as a person closing its window (the open note is saved first), runs [whileClosed] (another app
 * changing files meanwhile) and launches it again on the same data, waiting until the new one is up.
 */
fun CaseScope.relaunch(whileClosed: () -> Unit = {}) {
    awaitNewNotekea(quitting = CaseApps.quit(vaults = folders.data, whileClosed = whileClosed))
}

/** Closes the window with [shortcut] (the open note is saved first) and waits until Notekea is up again. */
fun CaseScope.closeWindowWith(shortcut: Shortcut) {
    val quitting = notekea()
    pressInWindow(shortcut = shortcut)
    awaitNewNotekea(quitting = quitting)
}

private fun CaseScope.awaitNewNotekea(quitting: CaseApp) {
    // Quitting waits for the window's close guard, which gives each of its handlers a few seconds.
    ui.waitUntil(timeoutMillis = RELAUNCH_MILLIS) {
        runCatching { CaseApps.of(vaults = folders.data) !== quitting }.getOrDefault(false)
    }
    ui.waitForIdle()
}

/**
 * Opens the vault [name] of the case's data with Open folder…, the picker answering it, until [shownText] shows and,
 * unless [waitsForIndex] is false (a vault too large to index in a case's wait), its notes are ready to search: the
 * inspector's sections then show what they show for good, so a case meets the same window every run.
 */
fun CaseScope.openVault(
    name: String,
    shownText: String,
    waitsForIndex: Boolean = true
) {
    notekea().folderPicker.nextPick = notekea().vaultPath(name = name)
    click(text = OPEN_FOLDER)
    // A loaded machine can take longer than a text's usual wait to read a whole vault (1.1-07 on Windows).
    ui.waitUntil(timeoutMillis = INDEX_MILLIS) { ui.onAllNodes(hasText(shownText)).fetchSemanticsNodes().isNotEmpty() }
    expectText(text = shownText)
    if (waitsForIndex) awaitIndex()
}

/** Waits for the index with [INDEX_MILLIS]: it runs the real model, which takes its time on a busy machine. */
private fun CaseScope.awaitIndex() {
    step(description = "Expect “$INDEX_READY”")
    try {
        ui.waitUntilAtLeastOneExists(
            matcher = hasText(text = INDEX_READY, substring = true),
            timeoutMillis = INDEX_MILLIS
        )
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected to see \"$INDEX_READY\", it did not appear").apply {
            initCause(exception)
        }
    }
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

/** Waits until the open palette (the quick switcher, a picker) lists [name] among its results. */
fun CaseScope.waitForResult(name: String) {
    try {
        ui.waitUntilAtLeastOneExists(matcher = paletteResult(name = name), timeoutMillis = WAIT_MILLIS)
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected the palette to list \"$name\", it did not").apply {
            initCause(exception)
        }
    }
}

/** Waits until the open palette no longer lists [name]; the same name elsewhere in the window does not count. */
fun CaseScope.expectNoResult(name: String) {
    step(description = "Expect no result “$name”")
    awaitNone(matcher = paletteResult(name = name), failure = "expected the palette to stop listing \"$name\"")
}

/** Waits until the tree shows no row with [text]; the same name elsewhere (a tab, the inspector) does not count. */
fun CaseScope.expectNotInTree(text: String) {
    step(description = "Expect no “$text” in the tree")
    awaitNone(matcher = inTree(text = text), failure = "expected \"$text\" to leave the tree, it stayed")
}

/** Waits until the note's text field no longer holds [part]; the same words elsewhere in the window do not count. */
fun CaseScope.expectNotInNoteText(part: String) {
    step(description = "Expect no “$part” in the note's text")
    val inField = hasText(text = part, substring = true) and hasSetTextAction()
    awaitNone(matcher = inField, failure = "expected the note's text to stop showing \"$part\"")
}

private fun paletteResult(
    name: String
): SemanticsMatcher = hasText(text = name) and !hasSetTextAction() and
    hasAnyAncestor(matcher = hasTestTag(testTag = TestTags.PALETTE_RESULTS))

private fun inTree(
    text: String
): SemanticsMatcher = hasText(text = text) and hasAnyAncestor(matcher = hasTestTag(testTag = TestTags.TREE))

private fun CaseScope.awaitNone(
    matcher: SemanticsMatcher,
    failure: String
) {
    try {
        ui.waitUntil(timeoutMillis = WAIT_MILLIS) {
            val nodes = ui.onAllNodes(matcher = matcher)
            (0 until nodes.fetchSemanticsNodes().size).none { nodes[it].isShown() }
        }
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = failure).apply { initCause(exception) }
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

/** The row of the sidebar's tree that shows [text], once it shows; the same name elsewhere is never taken for it. */
fun CaseScope.treeRow(text: String): SemanticsNodeInteraction {
    try {
        ui.waitUntilAtLeastOneExists(matcher = inTree(text = text), timeoutMillis = WAIT_MILLIS)
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = "expected the tree to show \"$text\", it did not").apply { initCause(exception) }
    }
    return ui.onAllNodes(matcher = inTree(text = text)).onFirst()
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
