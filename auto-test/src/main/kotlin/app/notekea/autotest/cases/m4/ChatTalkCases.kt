package app.notekea.autotest.cases.m4

import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m3.FIVE_MINUTES
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.TWO_AND_A_HALF
import app.notekea.autotest.cases.m3.ask
import app.notekea.autotest.cases.m3.copiedChat
import app.notekea.autotest.cases.m3.expectInChat
import app.notekea.autotest.cases.m3.openChat
import app.notekea.autotest.cases.m3.replyTo
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val WATER_ES = "¿cuánta agua llevan las arepas?"
private const val REST_FOLLOW_UP = "¿y cuánto reposa la masa?"
private const val REMINDER = "mandame un recordatorio mañana a las 9"

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

private fun SuiteBuilder.memoryRing() {
    manual(
        id = "4.7-06",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "The chat's memory ring fills as it talks, and Compact chat empties it into a summary")

        steps(
            text =
            "1. Open the Playground Vault and the chat (the speech bubble in the narrow bar on the far left).\n2. Ask " +
                "five questions about the recipes, one after another.\n3. Hold the pointer on the ring beside " +
                "Medium · Auto at the bottom right of the field.\n4. Open the chat's ⋯ menu and click Compact " +
                "chat.\n5. Ask: and the first recipe I asked about?"
        )

        expected(
            text =
            "The ring fills a little with each reply; its tooltip says how full the memory is. After Compact chat " +
                "the ring is nearly empty, and step 5 is still answered about the first recipe."
        )
    }
}
