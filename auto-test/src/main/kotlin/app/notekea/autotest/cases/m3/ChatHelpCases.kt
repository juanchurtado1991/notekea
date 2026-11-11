package app.notekea.autotest.cases.m3

import app.notekea.autotest.ChatModels
import com.testbuddy.CaseFailure
import com.testbuddy.SuiteBuilder

private const val HOW_TO_NOTE = "¿cómo creo una nota nueva?"
private const val WHAT_CAN = "what can you do?"
private const val WHAT_IS_IN = "explícame qué hay en las notas"
private const val IN_ENGLISH = "what is in my notes?"
private const val IN_SPANISH = "en español"

/**
 * The chat on how to use Notekea and on the vault as a whole (the developer's smoke of 2026-11-11), with a chat
 * model on the machine: explained from the app's guide with the system's shortcuts, never done.
 */
internal fun SuiteBuilder.chatHelpCases() {
    howTo()
    overview()
    sayAgain()
}

private fun SuiteBuilder.howTo() {
    automated(
        id = "3.3-09",
        title = "How to do something is explained with the system's shortcut and offered, never done",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = HOW_TO_NOTE)
        ask(question = WHAT_CAN)
        val chat = copiedChat()

        val howTo = replyTo(
            chat = chat,
            question = HOW_TO_NOTE
        )

        expectInChat(howTo, "Ctrl+N", "⌘N")
        expectInChat(howTo, "Outcome: answered")
        expectNoAction(reply = howTo)

        val whatCan = replyTo(
            chat = chat,
            question = WHAT_CAN
        )

        expectInChat(whatCan, "card", "Undo", "cite", "notes")
        expectNoAction(reply = whatCan)

        click(text = "Hazlo por mí")

        expectText(
            text = "create a note called",
            substring = true
        )
    }
}

private fun SuiteBuilder.overview() {
    automated(
        id = "3.3-10",
        title = "What is in the notes is told from the vault's folders, in the question's language",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = WHAT_IS_IN)

        val reply = replyTo(
            chat = copiedChat(),
            question = WHAT_IS_IN
        )

        expectInChat(reply, "Outcome: answered")
        expectInChat(reply, "Recipes", "Recetas", "recetas")
        expectInChat(reply, "Travel", "Viaje", "viaje")
        expectInChat(reply, " las ", " tus ", " notas")
        expectNoAction(reply = reply)
    }
}

private fun SuiteBuilder.sayAgain() {
    automated(
        id = "3.3-11",
        title = "\"en español\" says the last reply again in Spanish, without searching or acting",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = IN_ENGLISH)
        ask(question = IN_SPANISH)

        val reply = replyTo(
            chat = copiedChat(),
            question = IN_SPANISH
        )

        expectInChat(reply, "Outcome: answered")
        expectInChat(reply, " las ", " tus ", " notas")
        expectInChat(reply, "Recipes", "Recetas", "recetas", "Travel", "viaje")
        expectNoAction(reply = reply)
    }
}

/** Fails when [reply] (a [replyTo]) shows the chat planned or did an action: how to is explained, never done. */
private fun expectNoAction(reply: String) {
    if ("Action:" in reply) throw CaseFailure(message = "expected an explanation and no action, it acted:\n$reply")
}
