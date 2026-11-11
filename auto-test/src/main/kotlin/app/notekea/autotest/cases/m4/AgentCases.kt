package app.notekea.autotest.cases.m4

import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.ask
import app.notekea.autotest.cases.m3.openChat
import com.testbuddy.SuiteBuilder

private const val TASK = "find my recipe made with corn flour and tag it with latin"
private const val AREPAS = "$PLAYGROUND/Recipes/Arepas.md"

/**
 * The case of 4.7, agent mode (`MVP_SMOKE_TEST.md`), with Medium, the smallest size that offers it: a task that
 * needs a look-up lists its steps, ends on the card, and changes the note only once Do it is clicked.
 */
fun SuiteBuilder.agentCases() {
    automated(
        id = "4.7-01",
        title = "Agent mode lists its steps, ends on the card, and changes nothing before Do it",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat()
        clickIcon(label = "Agent mode")
        ask(question = TASK)

        expectText(
            text = "Searched the notes for",
            substring = true
        )

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
