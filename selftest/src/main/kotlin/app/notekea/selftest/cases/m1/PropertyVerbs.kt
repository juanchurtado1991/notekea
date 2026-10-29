package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import kotlin.math.abs

private const val WAIT_MILLIS = 10_000L

/** The first text field on screen showing exactly [text]: a property's name or value. */
internal fun CaseScope.field(text: String): SemanticsNodeInteraction =
    firstShownOf(matcher = hasSetTextAction() and hasText(text = text), what = "a field showing \"$text\"")

/** Replaces the text of the field showing [current] with [text] and presses Enter, as a person editing it. */
internal fun CaseScope.editField(
    current: String,
    text: String
) {
    field(text = current).performClick()
    // A field can show its value another way while it is edited (a date as 2026-11-02), so the focused one is used.
    val focused = ui.onNode(matcher = isFocused() and hasSetTextAction())
    focused.performTextReplacement(text = text)
    focused.performKeyInput { pressKey(key = Key.Enter) }
}

/**
 * The element matching [matcher] on the same row as the element showing [rowText] (the ✕ of a property, the empty
 * field of a list): of those shown, the one nearest to it, below or to its right.
 */
internal fun CaseScope.onRowOf(
    rowText: String,
    matcher: SemanticsMatcher,
    anchor: SemanticsMatcher = hasText(text = rowText)
): SemanticsNodeInteraction {
    val row = firstShownOf(matcher = anchor, what = "\"$rowText\"").fetchSemanticsNode().boundsInRoot
    val nodes = ui.onAllNodes(matcher = matcher)
    val count = nodes.fetchSemanticsNodes().size
    return (0 until count).map { nodes[it] }.filter { it.isDisplayed() }.minByOrNull { node ->
        val bounds = node.fetchSemanticsNode().boundsInRoot
        val rowDistance = abs(bounds.center.y - row.center.y) * ROW_WEIGHT
        rowDistance + if (bounds.left >= row.left) bounds.left - row.right else LEFT_PENALTY
    } ?: throw CaseFailure(message = "expected something next to \"$rowText\", there is nothing")
}

/** Clicks the button labeled [label] on the row of [rowText]. */
internal fun CaseScope.clickOnRowOf(
    rowText: String,
    label: String
) {
    onRowOf(rowText = rowText, matcher = hasContentDescription(value = label)).performClick()
}

/** Fails unless the file at [path] still has exactly [content]: opening or reading a note never writes. */
internal fun CaseScope.expectUnchanged(
    path: String,
    content: String
) {
    Thread.sleep(SAVE_PAUSE_MILLIS)
    expectFile(path = path, content = content)
}

private fun CaseScope.firstShownOf(
    matcher: SemanticsMatcher,
    what: String
): SemanticsNodeInteraction {
    var found: SemanticsNodeInteraction? = null
    runCatching {
        ui.waitUntil(timeoutMillis = WAIT_MILLIS) {
            val nodes = ui.onAllNodes(matcher = matcher)
            val count = nodes.fetchSemanticsNodes().size
            found = (0 until count).map { nodes[it] }.firstOrNull { it.isDisplayed() }
            found != null
        }
    }
    return found ?: throw CaseFailure(message = "expected $what, it did not appear")
}

// A row counts far more than a distance along it, and what is left of the anchor is never on its row.
private const val ROW_WEIGHT = 10f
private const val LEFT_PENALTY = 100_000f

// Longer than the editor's save delay (a second), so a change it should not make has had time to land.
private const val SAVE_PAUSE_MILLIS = 1_500L
