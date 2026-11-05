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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * What a case's body can do, in a person's words: click what shows a text, type, press a shortcut, and expect texts
 * and files. Every expectation waits up to [waitMillis] for the app, which works in the background, and fails with a
 * [CaseFailure] a tester can read. [ui] is Compose UI Test itself, for what the verbs do not cover. Each verb tells
 * [onStep] what it does (`Click “Personal Notes”`), which the manager shows while the case runs; a host's own verbs can
 * name a step of theirs with [step].
 */
@TestBuddyDsl
public class CaseScope internal constructor(
    public val ui: ComposeUiTest,
    public val folders: CaseFolders,
    public val system: HostSystem,
    private val waitMillis: Long,
    private val onStep: (String) -> Unit = {}
) {
    /** Says what the case does now, in a person's words, as the verbs do. */
    public fun step(description: String) {
        onStep(description)
    }

    private val files = CaseFiles(folders = folders, waitFor = { holdsInTime(condition = it) })

    /** Clicks the first element that shows [text] (or contains it, with [substring]), once it shows. */
    public fun click(
        text: String,
        substring: Boolean = false
    ) {
        step(description = "Click “$text”")
        awaitText(text = text, substring = substring)
        firstShown(matcher = hasText(text = text, substring = substring)).apply {
            runCatching { performScrollTo() }
            clickWhenStill()
        }
    }

    /** Right-clicks the first element that shows [text], once it shows, to open its context menu. */
    public fun rightClick(text: String) {
        step(description = "Right-click “$text”")
        awaitText(text = text, substring = false)
        ui.waitForIdle()
        firstShown(matcher = hasText(text = text)).performMouseInput { rightClick() }
    }

    /**
     * Clicks the words [words] inside a longer text, where they are drawn: a link in a paragraph, a word in a note.
     * The first text that contains them is used.
     */
    public fun clickWords(words: String) {
        step(description = "Click “$words”")
        awaitText(text = words, substring = true)
        // A text's links can follow it by a frame: a click before they settle lands on plain words.
        ui.waitForIdle()
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
        // The middle of the first word: words that wrap onto two lines have a line's end between them, not the link.
        val firstWord = words.substringBefore(delimiter = ' ')
        val box = layout.getBoundingBox(offset = text.indexOf(words) + firstWord.length / 2)
        node.performMouseInput { click(position = box.center) }
    }

    /** Clicks the first icon button labeled [label] for screen readers (its content description), once it shows. */
    public fun clickIcon(label: String) {
        step(description = "Click the $label button")
        val icon = hasContentDescription(value = label)
        expect(matcher = icon, failure = "expected a button labeled \"$label\", it did not appear")
        firstShown(matcher = icon).clickWhenStill()
    }

    /** Types [text] where the keyboard is: the focused text field. */
    public fun type(text: String) {
        step(description = "Type “$text”")
        val field = isFocused() and hasSetTextAction()
        expect(matcher = field, failure = "nothing was ready to type \"$text\" in: click a text field first")
        ui.onNode(matcher = field).performTextInput(text = text)
    }

    /** Empties the focused text field. */
    public fun clearText() {
        step(description = "Clear the field")
        val field = isFocused() and hasSetTextAction()
        expect(matcher = field, failure = "nothing was ready to clear: click a text field first")
        ui.onNode(matcher = field).performTextClearance()
    }

    /** Presses [shortcut] with this system's keys (`Mod` is Cmd on macOS, Ctrl elsewhere). */
    public fun press(shortcut: Shortcut) {
        step(description = "Press ${shortcut.describe(system = system)}")
        keyboardTarget().performKeyInput {
            withKeysDown(keys = shortcut.modifierKeysOn(system = system)) { pressKey(key = shortcut.key) }
        }
    }

    /** Presses [key] alone: Enter, Escape, an arrow. */
    public fun press(key: Key) {
        step(description = "Press ${keyName(key = key)}")
        keyboardTarget().performKeyInput { pressKey(key = key) }
    }

    /** Scrolls the list that holds [text] until it is in view, also when a lazy list has not made its row yet. */
    public fun scrollTo(text: String) {
        step(description = "Scroll to “$text”")
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
        awaitText(text = text, substring = false)
    }

    /** Waits until something shows [text] (or contains it, with [substring]). */
    public fun expectText(
        text: String,
        substring: Boolean = false
    ) {
        step(description = "Expect “$text”")
        awaitText(text = text, substring = substring)
    }

    /** Waits until [text] shows, without naming a step: what a verb does before acting on the text. */
    private fun awaitText(
        text: String,
        substring: Boolean
    ) {
        expect(
            matcher = hasText(text = text, substring = substring),
            failure = "expected to see \"$text\", it did not appear"
        )
    }

    public fun expectNoText(
        text: String,
        substring: Boolean = false
    ) {
        step(description = "Expect no “$text”")
        val holds = holdsInTime { !isTextShown(text = text, substring = substring) }
        if (!holds) throw CaseFailure(message = "expected \"$text\" to go away, it stayed")
    }

    /**
     * Watches for [settleMillis] that [text] never shows, for what would come late. [expectNoText] waits for a text
     * to leave and is done the moment it is gone, so a text that is yet to appear passes it.
     */
    public fun expectTextStaysAway(
        text: String,
        substring: Boolean = false,
        settleMillis: Long = DEFAULT_SETTLE_MILLIS
    ) {
        step(description = "Expect “$text” never to show")
        val appeared = holdsInTime(timeoutMillis = settleMillis) { isTextShown(text = text, substring = substring) }
        if (appeared) throw CaseFailure(message = "expected \"$text\" to stay away, it appeared")
    }

    private fun isTextShown(
        text: String,
        substring: Boolean
    ): Boolean {
        val nodes = ui.onAllNodes(matcher = hasText(text = text, substring = substring))
        val count = nodes.fetchSemanticsNodes().size
        // A node can leave between counting and looking at it: gone counts as not shown.
        return (0 until count).any { nodes[it].isShown() }
    }

    /** The file at [path] in the case's data folder (`"Personal Notes/Welcome.md"`), to read or change. */
    public fun file(path: String): Path = files.resolve(path = path)

    /** Waits until the file at [path] exists and, when given, has exactly [content] or contains [containing]. */
    public fun expectFile(
        path: String,
        content: String? = null,
        containing: String? = null
    ) {
        step(description = "Expect the file “$path”")
        files.expectFile(path = path, content = content, containing = containing)
    }

    /**
     * Watches for [settleMillis] that the file at [path] keeps exactly [content], for what must not be saved, as a
     * save comes a moment after the action.
     */
    public fun expectFileStays(
        path: String,
        content: String,
        settleMillis: Long = DEFAULT_SETTLE_MILLIS
    ) {
        step(description = "Expect the file “$path” to stay as it is")
        val file = files.resolve(path = path)
        val changed = holdsInTime(timeoutMillis = settleMillis) { Files.readString(file) != content }
        if (changed) throw CaseFailure(message = "expected \"$path\" to stay as it was, it changed")
    }

    /** Lets the app run for [millis], for what no text tells has ended, as a scroll coming back to its place. */
    public fun settle(millis: Long = DEFAULT_SETTLE_MILLIS) {
        holdsInTime(timeoutMillis = millis) { false }
    }

    /** Waits until no file is at [path]. */
    public fun expectNoFile(path: String) {
        step(description = "Expect no file “$path”")
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

    /**
     * Clicks this element once it has stopped moving. What opened just now may not take a click yet, and a panel still
     * filling in can move it between the press and the release, which leaves a press with no click.
     */
    public fun SemanticsNodeInteraction.clickWhenStill() {
        ui.waitForIdle()
        var bounds = fetchSemanticsNode().boundsInRoot
        var stillSince = TimeSource.Monotonic.markNow()
        holdsInTime {
            val now = runCatching { fetchSemanticsNode().boundsInRoot }.getOrNull() ?: bounds
            if (now != bounds) {
                bounds = now
                stillSince = TimeSource.Monotonic.markNow()
            }
            stillSince.elapsedNow() >= STILL_TIME
        }
        performClick()
    }

    private fun firstShown(matcher: SemanticsMatcher): SemanticsNodeInteraction {
        val nodes = ui.onAllNodes(matcher = matcher)
        val count = nodes.fetchSemanticsNodes().size
        return (0 until count).map { nodes[it] }.firstOrNull { it.isShown() } ?: nodes.onFirst()
    }

    private fun holdsInTime(
        timeoutMillis: Long = waitMillis,
        condition: () -> Boolean
    ): Boolean = try {
        ui.waitUntil(timeoutMillis = timeoutMillis, condition = condition)
        true
    } catch (_: ComposeTimeoutException) {
        false
    }

    private fun keyboardTarget(): SemanticsNodeInteraction {
        val focused = ui.onAllNodes(matcher = isFocused())
        val hasFocus = focused.fetchSemanticsNodes().isNotEmpty()
        return if (hasFocus) focused.onFirst() else ui.onAllNodes(matcher = isRoot()).onFirst()
    }

    private companion object {
        // Long enough for what a click sets off in the background to reach the screen.
        const val DEFAULT_SETTLE_MILLIS = 2_000L
        val STILL_TIME = 150.milliseconds
    }
}
