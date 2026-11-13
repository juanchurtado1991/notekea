package app.notekea.chat.domain

import app.notekea.chat.domain.language.ReplyLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplyLanguageTest {
    @Test
    fun `the chat replies in Spanish to Spanish, and in English otherwise`() {
        assertEquals(
            expected = listOf(
                ReplyLanguage.SPANISH,
                ReplyLanguage.SPANISH,
                ReplyLanguage.ENGLISH,
                ReplyLanguage.ENGLISH
            ),
            actual = listOf(
                ReplyLanguage.of(text = "borra todas mis notas para siempre"),
                ReplyLanguage.of(text = "¿Cuánta agua lleva la masa?"),
                ReplyLanguage.of(text = "delete all my notes permanently"),
                ReplyLanguage.of(text = "Dune")
            )
        )
    }
}
