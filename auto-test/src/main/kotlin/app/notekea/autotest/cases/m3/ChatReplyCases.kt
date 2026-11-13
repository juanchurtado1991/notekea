package app.notekea.autotest.cases.m3

import app.notekea.autotest.ChatModels
import app.notekea.autotest.openNote
import com.testbuddy.CaseFailure
import com.testbuddy.SuiteBuilder

private const val DENTIST = "When is my dentist appointment?"
private const val WATER_ES = "¿Cuánta agua lleva la masa de las arepas?"
private const val HELLO = "hola"
private const val HOW_TO = "How do I make arepas?"
private const val FOLLOW_UP = "and how long does it rest?"
private const val OPINION = "¿qué opinas de este viaje?"
private const val TRIPS = "what trips do I have?"

/** The cases of 3.3 on what the chat replies (`MVP_SMOKE_TEST.md`), with a chat model on the machine. */
internal fun SuiteBuilder.chatReplyCases() {
    notFound()
    talkAndFollowUps()
    copyChat()
}

private fun SuiteBuilder.notFound() {
    automated(
        id = "3.3-05",
        title = "A question the notes do not answer is declined, in the question's language",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat()
        ask(question = DENTIST)
        expectText(text = NOT_FOUND)
        ask(question = WATER_ES)
        val chat = copiedChat()

        val dentist = replyTo(
            chat = chat,
            question = DENTIST
        )

        expectInChat(dentist, "Outcome: not_found")
        expectNoSources(reply = dentist)

        val water = replyTo(
            chat = chat,
            question = WATER_ES
        )

        expectInChat(water, *TWO_AND_A_HALF)
        expectInChat(water, " tazas", " taza")
        expectInChat(water, "] Recipes/Arepas")
    }
}

private fun SuiteBuilder.talkAndFollowUps() {
    automated(
        id = "3.3-07",
        title = "The chat talks, follows the conversation and looks further",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat()
        ask(question = HELLO)
        ask(question = HOW_TO)
        ask(question = FOLLOW_UP)

        openNote(
            name = "Lisbon trip",
            shownText = "Alfama walk"
        )

        ask(question = OPINION)
        ask(question = TRIPS)
        val chat = copiedChat()

        val hello = replyTo(
            chat = chat,
            question = HELLO
        )

        expectInChat(hello, "Outcome: answered")
        expectInChat(hello, "hola", "¡")
        expectNoSources(reply = hello)

        expectInChat(
            replyTo(
                chat = chat,
                question = FOLLOW_UP
            ),
            *FIVE_MINUTES
        )

        val opinion = replyTo(
            chat = chat,
            question = OPINION
        )

        expectInChat(opinion, "] Travel/Lisbon trip")

        expectLanguage(
            reply = opinion,
            language = "Spanish"
        )

        expectInChat(
            replyTo(
                chat = chat,
                question = TRIPS
            ),
            "Lisbon",
            "Lisboa"
        )
    }
}

private fun SuiteBuilder.copyChat() {
    automated(
        id = "3.3-08",
        title = "Copy chat puts the whole conversation on the clipboard",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat()
        ask(question = WATER)
        ask(question = REST)
        val chat = copiedChat()
        expectInChat(chat, "# Notekea chat")

        listOf(WATER, REST).forEach { question ->
            val reply = replyTo(
                chat = chat,
                question = question
            )

            expectInChat(reply, "Outcome: answered")
            expectInChat(reply, "Sources:")
            expectInChat(reply, "] Recipes/Arepas")
        }
    }
}

/** Fails if [reply] (a [replyTo]) cites any note. */
private fun expectNoSources(reply: String) {
    if (reply.contains(other = "Sources:")) {
        throw CaseFailure(message = "expected no sources, the reply cited some:\n$reply")
    }
}
