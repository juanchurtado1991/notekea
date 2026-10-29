package com.testbuddy

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.click
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.test.withKeysDown
import java.nio.file.Path

/**
 * What a case's body can do, in a person's words: click what shows a text, type, press a shortcut, and expect texts
 * and files. Every expectation waits up to [waitMillis] for the app, which works in the background, and fails with a
 * [CaseFailure] a tester can read. [ui] is Compose UI Test itself, for what the verbs do not cover.
 */
@TestBuddyDsl
public class CaseScope internal constructor(
    public val ui: ComposeUiTest,
    public val folders: CaseFolders,
    public val system: HostSystem,
    private val waitMillis: Long
) {
    private val files = CaseFiles(folders = folders, waitFor = ::holdsInTime)

    /** Clicks the first element that shows [text] (or contains it, with [substring]), once it shows. */
    public fun click(
        text: String,
        substring: Boolean = false
    ) {
        expectText(text = text, substring = substring)
        firstShown(matcher = hasText(text = text, substring = substring)).apply {
            runCatching { performScrollTo() }
            performClick()
        }
    }

    /** Right-clicks the first element that shows [text], once it shows, to open its context menu. */
    public fun rightClick(text: String) {
        expectText(text = text)
        firstShown(matcher = hasText(text = text)).performMouseInput { rightClick() }
    }

    /**
     * Clicks the words [words] inside a longer text, where they are drawn: a link in a paragraph, a word in a note.
     * The first text that contains them is used.
     */
    public fun clickWords(words: String) {
        expectText(text = words, substring = true)
        val node = firstShown(matcher = hasText(text = words, substring = true))
        // A person scrolls to what they click; a text in a scrolling view may be below the window.
        runCatching { node.performScrollTo() }
        val semantics = node.fetchSemanticsNode()
        val shown = (
            semantics.config.getOrNull(SemanticsProperties.Text).orEmpty() +
                listOfNotNull(semantics.config.getOrNull(SemanticsProperties.EditableText))
            ).map { it.text }
        val text = shown.first { it.contains(words) }
        val layouts = mutableListOf<TextLayoutResult>()
        semantics.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        val layout =
            layouts.firstOrNull() ?: throw CaseFailure(message = "\"$words\" is not in a text that can be clicked")
        val box = layout.getBoundingBox(offset = text.indexOf(words) + words.length / 2)
        node.performMouseInput { click(position = box.center) }
    }

    /** Clicks the first icon button labeled [label] for screen readers (its content description), once it shows. */
    public fun clickIcon(label: String) {
        val icon = hasContentDescription(value = label)
        expect(matcher = icon, failure = "expected a button labeled \"$label\", it did not appear")
        firstShown(matcher = icon).performClick()
    }

    /** Types [text] where the keyboard is: the focused text field. */
    public fun type(text: String) {
        val field = isFocused() and hasSetTextAction()
        expect(matcher = field, failure = "nothing was ready to type \"$text\" in: click a text field first")
        ui.onNode(matcher = field).performTextInput(text = text)
    }

    /** Empties the focused text field. */
    public fun clearText() {
        val field = isFocused() and hasSetTextAction()
        expect(matcher = field, failure = "nothing was ready to clear: click a text field first")
        ui.onNode(matcher = field).performTextClearance()
    }

    /** Presses [shortcut] with this system's keys (`Mod` is Cmd on macOS, Ctrl elsewhere). */
    public fun press(shortcut: Shortcut) {
        keyboardTarget().performKeyInput {
            withKeysDown(keys = shortcut.modifierKeysOn(system = system)) { pressKey(key = shortcut.key) }
        }
    }

    /** Presses [key] alone: Enter, Escape, an arrow. */
    public fun press(key: Key) {
        keyboardTarget().performKeyInput { pressKey(key = key) }
    }

    /** Scrolls the list that holds [text] until it is in view, also when a lazy list has not made its row yet. */
    public fun scrollTo(text: String) {
        val target = hasText(text = text)
        val isMade = ui.onAllNodes(matcher = target).fetchSemanticsNodes().isNotEmpty()
        if (isMade) {
            ui.onAllNodes(matcher = target).onFirst().performScrollTo()
            return
        }
        val lists = ui.onAllNodes(matcher = hasScrollToNodeAction())
        val count = lists.fetchSemanticsNodes().size
        val list = (0 until count).firstOrNull { index ->
            runCatching { lists[index].performScrollToNode(matcher = target) }.isSuccess
        }
        if (list == null) throw CaseFailure(message = "expected a list holding \"$text\", none had it")
        expectText(text = text)
    }

    /** Waits until something shows [text] (or contains it, with [substring]). */
    public fun expectText(
        text: String,
        substring: Boolean = false
    ) {
        expect(
            matcher = hasText(text = text, substring = substring),
            failure = "expected to see \"$text\", it did not appear"
        )
    }

    /** Waits until nothing shows [text] (or a text containing it, with [substring]) on screen: gone, or hidden. */
    public fun expectNoText(
        text: String,
        substring: Boolean = false
    ) {
        val holds = holdsInTime {
            val nodes = ui.onAllNodes(matcher = hasText(text = text, substring = substring))
            val count = nodes.fetchSemanticsNodes().size
            (0 until count).none { nodes[it].isDisplayed() }
        }
        if (!holds) throw CaseFailure(message = "expected \"$text\" to go away, it stayed")
    }

    /** The file at [path] in the case's data folder (`"Personal Notes/Welcome.md"`), to read or change. */
    public fun file(path: String): Path = files.resolve(path = path)

    /** Waits until the file at [path] exists and, when given, has exactly [content] or contains [containing]. */
    public fun expectFile(
        path: String,
        content: String? = null,
        containing: String? = null
    ) {
        files.expectFile(path = path, content = content, containing = containing)
    }

    /** Waits until no file is at [path]. */
    public fun expectNoFile(path: String) {
        files.expectNoFile(path = path)
    }

    private fun expect(
        matcher: SemanticsMatcher,
        failure: String
    ) {
        try {
            ui.waitUntilAtLeastOneExists(matcher = matcher, timeoutMillis = waitMillis)
        } catch (exception: ComposeTimeoutException) {
            throw CaseFailure(message = failure).apply { initCause(exception) }
        }
    }

    // The first element matching [matcher] that is on screen: a panel being hidden (the editor while Preview takes its
    // place) can still hold the same text, and a person only clicks what they see.
    private fun firstShown(matcher: SemanticsMatcher): SemanticsNodeInteraction {
        val nodes = ui.onAllNodes(matcher = matcher)
        val count = nodes.fetchSemanticsNodes().size
        return (0 until count).map { nodes[it] }.firstOrNull { it.isDisplayed() } ?: nodes.onFirst()
    }

    private fun holdsInTime(condition: () -> Boolean): Boolean = try {
        ui.waitUntil(timeoutMillis = waitMillis, condition = condition)
        true
    } catch (_: ComposeTimeoutException) {
        false
    }

    private fun keyboardTarget(): SemanticsNodeInteraction {
        val focused = ui.onAllNodes(matcher = isFocused())
        val hasFocus = focused.fetchSemanticsNodes().isNotEmpty()
        return if (hasFocus) focused.onFirst() else ui.onAllNodes(matcher = isRoot()).onFirst()
    }
}
