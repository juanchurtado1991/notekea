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

    // The bug hunt of 2026-11-17 (E8): "usa" (the verb) and "chile" (the food) were countries, and the first country
    // was the first of the list, not of the text.
    @Test
    fun `a name that is also a word is a country only after a preposition, and the first named wins`() {
        assertEquals(
            expected = listOf("CO", null, "CL", "US", "US", "PE"),
            actual = listOf(
                "usa la web para ver el precio del café en Colombia",
                "una salsa de chile para el chile relleno",
                "vivo en Chile",
                "where to buy it in the USA",
                "precio en EE. UU.",
                "vuelos de Perú a Colombia"
            ).map { Countries.codeIn(text = it) }
        )
    }
}
