package app.notekea.cards.eval

import kotlin.test.Test
import kotlin.test.assertEquals

/** The sentences the cards' judge reads of a note: prose, list items and rows, nothing else. */
class NoteSentencesTest {
    @Test
    fun `prose, items and rows are cut into sentences, and properties, headings, code and cards left out`() {
        val text = """
            |---
            |tags: [salud]
            |---
            |# Abuelo Hernando
            |
            |Vive en Usaquén con su perrita Canela. Tiene diabetes tipo 2.
            |- 2026-11-27 — endocrinóloga, en ayunas
            || Metformina | 850 mg | con el desayuno |
            ||---|---|---|
            |```
            |code that is no sentence at all
            |```
            |Capital of Peru::Lima
            |[[Turnos de visitas]]
        """.trimMargin()

        assertEquals(
            expected = listOf(
                "Vive en Usaquén con su perrita Canela.",
                "Tiene diabetes tipo 2.",
                "2026-11-27 — endocrinóloga, en ayunas",
                "Metformina ; 850 mg ; con el desayuno"
            ),
            actual = NoteSentences.of(text = text)
        )
    }
}
