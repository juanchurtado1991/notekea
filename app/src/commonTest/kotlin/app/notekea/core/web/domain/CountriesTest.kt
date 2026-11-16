package app.notekea.core.web.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** The country a web search is made in (`docs/agent-web.md`, "The region"). */
class CountriesTest {
    @Test
    fun `a country named in English or Spanish, with or without its accents, is its code, and none is null`() {
        assertEquals(
            expected = listOf("MX", "CO", "ES", "CR", null),
            actual = listOf(
                "where can I buy a French press in Mexico?",
                "I live in Bogotá, Colombia",
                "¿dónde compro una cafetera en España?",
                "precio del café en Costa Rica",
                "where can I buy a French press?"
            ).map { Countries.codeIn(text = it) }
        )
    }
}
