package app.notekea.autotest

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.isShown

private const val OUTLINE_LABEL = "Headings of this note"
private const val WAIT_MILLIS = 10_000L

/** A heading's row in the outline: the note itself shows the same words. */
fun inOutline(heading: String): SemanticsMatcher = hasText(text = heading) and
    hasAnyAncestor(matcher = hasContentDescription(value = OUTLINE_LABEL))

/** Waits until the outline lists [heading]. */
fun CaseScope.expectInOutline(heading: String) {
    step(description = "Expect “$heading” in the outline")

    awaitOutline(
        failure = "expected \"$heading\" in the outline, it did not appear"
    ) { shown(heading = heading) }
}

/** Waits until the outline no longer lists [heading], or is hidden; the same words in the note do not count. */
fun CaseScope.expectNotInOutline(heading: String) {
    step(description = "Expect no “$heading” in the outline")

    awaitOutline(
        failure = "expected \"$heading\" to leave the outline, it stayed"
    ) {
        !shown(heading = heading)
    }
}

private fun CaseScope.shown(heading: String): Boolean {
    val nodes = ui.onAllNodes(matcher = inOutline(heading = heading))

    return (0 until nodes.fetchSemanticsNodes().size).any { nodes[it].isShown() }
}

private fun CaseScope.awaitOutline(
    failure: String,
    condition: () -> Boolean
) {
    try {
        ui.waitUntil(
            timeoutMillis = WAIT_MILLIS,
            condition = condition
        )
    } catch (exception: ComposeTimeoutException) {
        throw CaseFailure(message = failure).apply { initCause(exception) }
    }
}
