package app.notekea.autotest

import app.notekea.autotest.cases.m3.languageOf
import kotlin.test.Test
import kotlin.test.assertEquals

/** How a chat case tells the language of a reply (case 3.3-07 failed a Spanish reply, 2026-11-13). */
class ChatLanguageTest {
    @Test
    fun `a reply in Spanish with no el, la or es is Spanish, whatever language its sources are in`() {
        assertEquals(
            expected = "Spanish",
            actual = languageOf(reply = REPLY)
        )
    }

    private companion object {
        const val REPLY = "## ¿qué opinas de este viaje?\n\nScope: vault\nOutcome: answered\n" +
            "Answered with: fast\n\nEn mi opinión, este viaje a Lisboa parece muy interesante.\n\nSources:\n" +
            "[1] Travel/Lisbon trip.md:  # Lisbon trip  | Day | Plan | Alfama walk, the tram and the monastery"
    }
}
