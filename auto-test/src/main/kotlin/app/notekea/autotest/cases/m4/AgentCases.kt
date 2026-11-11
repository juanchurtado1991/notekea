package app.notekea.autotest.cases.m4

import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.ask
import app.notekea.autotest.cases.m3.chooseMode
import app.notekea.autotest.cases.m3.copiedChat
import app.notekea.autotest.cases.m3.expectInChat
import app.notekea.autotest.cases.m3.openChat
import app.notekea.autotest.cases.m3.replyTo
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE_VAULT = "Sample Vault"
private const val NOTES_ABOUT = "me podes contar de que tratan las notas?"
private const val TASK = "find my recipe made with corn flour and tag it with latin"
private const val AREPAS = "$PLAYGROUND/Recipes/Arepas.md"

/**
 * The case of 4.7, agent mode (`MVP_SMOKE_TEST.md`), with Medium, the smallest size that offers it: a task that
 * needs a look-up lists its steps, ends on the card, and changes the note only once Do it is clicked.
 */
fun SuiteBuilder.agentCases() {
    agentWorks()
    overviewInAgentMode()
    notOnSmall()
    autoPicks()
}

private fun SuiteBuilder.agentWorks() {
    automated(
        id = "4.7-01",
        title = "Agent mode lists its steps, ends on the card, and changes nothing before Do it",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat()
        chooseMode(mode = "Agent")
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

// The developer's chat of 2026-11-11: in Agent mode "what are my notes about?" was searched for the word "notes".
private fun SuiteBuilder.overviewInAgentMode() {
    automated(
        id = "4.7-03",
        title = "In Agent mode, what the notes are about is told from the vault's folders, not searched",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat(withArepas = false)
        chooseMode(mode = "Agent")
        ask(question = NOTES_ABOUT)

        val reply = replyTo(
            chat = copiedChat(),
            question = NOTES_ABOUT
        )

        expectInChat(reply, "Answered with: fast")
        expectInChat(reply, "Recipes", "Recetas", "recetas")
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
            "Under the question field it says Small · Auto, and its menu offers only Auto and Fast. Step 3 is " +
                "answered from the notes, with its sources, as before."
        )
    }
}

private fun SuiteBuilder.autoPicks() {
    manual(
        id = "4.7-04",
        reason = ManualReason.COSTLY
    ) {
        prepare(SAMPLE_VAULT)
        title(text = "Auto thinks deeply about a comparison and hands the agent a task whose note it must find")

        steps(
            text =
            "1. With the Medium model, open the Sample Vault and the chat; leave the mode at Medium · Auto.\n2. Ask: " +
                "compare Deep Work and Atomic Habits\n3. Ask: add a task 'buy beans' to the recipe that uses black " +
                "beans\n4. Open the chat's ⋯ menu and Copy chat; paste it anywhere."
        )

        expected(
            text =
            "Step 2 says it is writing drafts and comparing them; step 3 lists the agent's steps and ends on a card " +
                "that adds the task to Black beans. The copied chat says Answered with: deep for step 2 and Answered " +
                "with: agent for step 3."
        )
    }
}
