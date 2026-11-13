package app.notekea.autotest.cases.m3

import androidx.compose.ui.test.hasContentDescription
import app.notekea.autotest.ChatModels
import com.testbuddy.SuiteBuilder

private const val REMOVE = "Don’t share with the chat"
private const val GONE_MILLIS = 10_000L

/**
 * The chat knows where the person is (the developer's choice of 2026-11-13): a chip over the question field names the
 * open note, and its × keeps it from the chat.
 */
internal fun SuiteBuilder.chatWorkCases() {
    automated(
        id = "3.3-12",
        title = "A chip over the chat's field names the open note, and its x takes it out of the chat's context",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat()
        expectText(text = "Arepas")
        clickIcon(label = REMOVE)

        ui
            .waitUntil(timeoutMillis = GONE_MILLIS) {
                ui
                    .onAllNodes(matcher = hasContentDescription(value = REMOVE))
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
    }
}
