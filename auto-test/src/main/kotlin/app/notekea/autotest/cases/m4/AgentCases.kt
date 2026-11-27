package app.notekea.autotest.cases.m4

import androidx.compose.ui.test.hasText
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.ask
import app.notekea.autotest.cases.m3.copiedChat
import app.notekea.autotest.cases.m3.expectInChat
import app.notekea.autotest.cases.m3.openChat
import app.notekea.autotest.cases.m3.replyTo
import com.testbuddy.SuiteBuilder

private const val NOTES_ABOUT = "me podes contar de que tratan las notas?"
private const val TASK = "find my recipe made with corn flour and tag it with latin"
private const val AREPAS = "$PLAYGROUND/Recipes/Arepas.md"
private const val STEP_MILLIS = 60_000L
private val STEPS = listOf("Read Recipes/Arepas", "Searched the notes", "Listed notes")

/**
 * The cases of 4.7, the agent (`MVP_SMOKE_TEST.md`), with Medium: a task that needs a look-up lists its steps, ends on
 * the card, and changes the note only once Do it is clicked.
 */
fun SuiteBuilder.agentCases() {
    agentWorks()
    overviewByTheLoop()
}

private fun SuiteBuilder.agentWorks() {
    automated(
        id = "4.7-01",
        title = "The agent lists its steps, ends on the card, and changes nothing before Do it",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat()
        ask(question = TASK)

        // Its steps vary (a search, notes listed, the recipe read): one of them is listed before the card.
        ui
            .waitUntil(timeoutMillis = STEP_MILLIS) {
                STEPS.any { step ->
                    ui
                        .onAllNodes(
                            matcher = hasText(
                                text = step,
                                substring = true
                            )
                        )
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }
            }

        expectText(text = "Do it")

        expectFileStays(
            path = AREPAS,
            content = file(path = AREPAS).toFile().readText()
        )

        click(text = "Do it")

        expectFile(
            path = AREPAS,
            containing = "latin"
        )
    }
}

// The developer's chat of 2026-11-11: with the agent "what are my notes about?" was searched for the word "notes".
private fun SuiteBuilder.overviewByTheLoop() {
    automated(
        id = "4.7-03",
        title = "What the notes are about is told from the vault's folders, not searched",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat(withArepas = false)
        ask(question = NOTES_ABOUT)

        val reply = replyTo(
            chat = copiedChat(),
            question = NOTES_ABOUT
        )

        expectInChat(reply, "Recipes", "Recetas", "recetas")
    }
}
