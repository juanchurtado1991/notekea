package app.notekea.autotest.cases.m4

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m3.FIVE_MINUTES
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.TWO_AND_A_HALF
import app.notekea.autotest.cases.m3.ask
import app.notekea.autotest.cases.m3.copiedChat
import app.notekea.autotest.cases.m3.expectInChat
import app.notekea.autotest.cases.m3.openChat
import app.notekea.autotest.cases.m3.replyTo
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.SuiteBuilder

private const val WATER_ES = "¿cuánta agua llevan las arepas?"
private const val REST_FOLLOW_UP = "¿y cuánto reposa la masa?"
private const val REMINDER = "mandame un recordatorio mañana a las 9"
private const val FIRST_RECIPE = "and the first recipe I asked about, how much water did it take?"
private const val RING = "Chat memory "
private const val MORE = "More"
private const val COMPACT = "Compact chat"
private const val COMPACT_MILLIS = 120_000L
private val QUESTIONS = listOf(
    "How much water do the arepas need?",
    "How long does the dough rest?",
    "How many servings do the black beans make?",
    "What is in the café con leche?",
    "What is on the weekly menu on Monday?",
    "What goes in the tomato soup?"
)

/**
 * The chat as the developer's probe of 2026-11-11 talked to it (`MVP_SMOKE_TEST.md`): it replies in the language it
 * is asked in, follows a follow-up, says plainly what Notekea cannot do, and keeps its memory in a ring.
 */
fun SuiteBuilder.chatTalkCases() {
    spanishFollowUp()
    memoryRing()
}

private fun SuiteBuilder.spanishFollowUp() {
    automated(
        id = "4.7-05",
        title = "The chat answers a Spanish question and its follow-up in Spanish, and refuses a reminder plainly",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat(withArepas = false)
        ask(question = WATER_ES)
        ask(question = REST_FOLLOW_UP)
        ask(question = REMINDER)

        val chat = copiedChat()

        expectInChat(replyTo(chat, WATER_ES), *TWO_AND_A_HALF)
        expectInChat(replyTo(chat, REST_FOLLOW_UP), *FIVE_MINUTES)
        expectInChat(replyTo(chat, REMINDER), "no puede", "no puedo")
    }
}

// The developer's goal of 2026-11-11: a ring like Claude Code's context, compacted when asked or when full.
private fun SuiteBuilder.memoryRing() {
    automated(
        id = "4.7-06",
        title = "The chat's memory ring fills as it talks, and Compact chat writes a summary without filling it more",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat(withArepas = false)

        val filled = QUESTIONS.map {
            ask(question = it)
            ringPercent()
        }

        expect(
            filled.zipWithNext().all { (before, after) ->
                after > before
            }
        ) { "expected the ring to fill with each reply, it read $filled" }

        ui.onNode(matcher = hasContentDescription(value = MORE)).performClick()
        click(text = COMPACT)

        // Compacting ends (ringPercent waits for it) and never fills the memory more: a short chat whose summary
        // would be no shorter stays as it is.
        val compacted = ringPercent()

        expect(compacted <= filled.last()) {
            "expected Compact chat not to fill the ring more, it read ${filled.last()} then $compacted"
        }

        ask(question = FIRST_RECIPE)
        expectInChat(replyTo(copiedChat(), FIRST_RECIPE), "arepa")
    }
}

/** The ring's share, as it says it to a screen reader ("Chat memory 42 % · …"), once a compaction under way ends. */
private fun CaseScope.ringPercent(): Int {
    runCatching {
        ui
            .waitUntil(timeoutMillis = COMPACT_MILLIS) {
                saidOnScreen().any { it.startsWith(prefix = RING) }
            }
    }

    val said = saidOnScreen()

    val ring = said.firstOrNull { it.startsWith(prefix = RING) }
        ?: throw CaseFailure(message = "expected the memory ring, the screen said: $said")

    return ring
        .substringAfter(delimiter = RING)
        .substringBefore(delimiter = " ")
        .toInt()
}

private fun CaseScope.saidOnScreen(): List<String> = ui
    .onAllNodes(
        matcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription),
        useUnmergedTree = true
    )
    .fetchSemanticsNodes()
    .flatMap {
        it.config[SemanticsProperties.ContentDescription]
    }

private fun expect(
    holds: Boolean,
    message: () -> String
) {
    if (!holds) throw CaseFailure(message = message())
}
