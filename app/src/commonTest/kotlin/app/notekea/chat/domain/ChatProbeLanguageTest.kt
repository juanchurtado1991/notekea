package app.notekea.chat.domain

import app.notekea.chat.domain.language.SpokenLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

/** The chat probe's rounds about language: which language a reply is in. */
class ChatProbeLanguageTest {
    @Test
    fun `a message too short to tell is in the chat's language, and a greeting tells alone`() {
        val spanish = listOf(
            ChatExchange(
                question = "¿cuánta agua llevan las arepas?",
                reply = "Dos tazas."
            )
        )

        assertEquals(
            expected = listOf("Spanish", "Spanish", "French", null),
            actual = listOf(
                SpokenLanguage.inChat(
                    text = "ok!",
                    history = spanish
                ),
                SpokenLanguage.inChat(
                    text = "gracias!",
                    history = emptyList()
                ),
                SpokenLanguage.inChat(
                    text = "merci",
                    history = spanish
                ),
                SpokenLanguage.inChat(
                    text = "ok!",
                    history = emptyList()
                )
            )
        )
    }
}
