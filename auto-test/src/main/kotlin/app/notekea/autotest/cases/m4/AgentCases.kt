package app.notekea.autotest.cases.m4

import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.ask
import app.notekea.autotest.cases.m3.openChat
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val TASK = "find my recipe made with corn flour and tag it with latin"
private const val AREPAS = "$PLAYGROUND/Recipes/Arepas.md"

/**
 * The case of 4.7, agent mode (`MVP_SMOKE_TEST.md`), with Medium, the smallest size that offers it: a task that
 * needs a look-up lists its steps, ends on the card, and changes the note only once Do it is clicked.
 */
fun SuiteBuilder.agentCases() {
    agentWorks()
    notOnSmall()
}

private fun SuiteBuilder.agentWorks() {
    automated(
        id = "4.7-01",
        title = "Agent mode lists its steps, ends on the card, and changes nothing before Do it",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat()
        clickIcon(label = "Agent mode")
        ask(question = TASK)

        // Its steps vary (a search or notes listed), but it reads the recipe before it proposes.
        expectText(
            text = "Read Recipes/Arepas",
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

private fun SuiteBuilder.notOnSmall() {
    manual(
        id = "4.7-02",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "With the Small model, Agent mode is not offered and the chat works as before")

        steps(
            text =
            "1. In Settings → AI model choose Small (download it if needed).\n2. Open the Playground Vault and the " +
                "chat (the speech bubble in the narrow bar on the far left).\n3. Ask: what do my recipes need?"
        )

        expected(
            text =
            "Under the question field there is no Agent mode checkbox. Step 3 is answered from the notes, with " +
                "its sources, as before."
        )
    }
}
