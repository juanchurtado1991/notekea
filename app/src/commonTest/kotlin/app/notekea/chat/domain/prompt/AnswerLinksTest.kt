package app.notekea.chat.domain.prompt

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An answer links only where the turn has been: the 4B ended nearly every answer from the notes with a link it made
 * up, "[site](https://example.com/greenhouse)" (the loop's fifth run, 2026-11-16).
 */
class AnswerLinksTest {
    @Test
    fun `a link to a site the turn never saw is taken out, and a page read keeps its link`() {
        val known =
            "read \"Greenhouse\": status active\nfetch https://en.wikipedia.org/wiki/Lisbon: 545,000"

        assertEquals(
            expected = listOf(
                "The status is **active**.",
                "The total is **9,600 USD**, in Budget 2026.",
                "Rosa's phone is **8855-2310**.",
                "Lisbon has 545,000 people [Wikipedia](https://en.wikipedia.org/wiki/Lisbon)."
            ),
            actual = listOf(
                "The status is **active** [site](https://example.com/greenhouse).",
                "The total is **9,600 USD**, in [Budget 2026](https://example.com/budget-2026).",
                "Rosa's phone is **8855-2310** [read \"Rosa Quesada\"](https://example.com/notes/Rosa%20Quesada).",
                "Lisbon has 545,000 people [Wikipedia](https://en.wikipedia.org/wiki/Lisbon)."
            ).map {
                AnswerLinks.kept(
                    answer = it,
                    known = known
                )
            }
        )
    }

    // The code review of 2026-11-22: the site was cut only at `/`, so a page read at an address
    // with a query and no path was a site of its own, and the links to it were taken out.
    @Test
    fun `a link to a site the turn read stays, however that address ended or was written`() {
        val answer = "It grows 2.3 million tonnes [FAO](https://www.fao.org/data)."

        assertEquals(
            expected = answer,
            actual = AnswerLinks.kept(
                answer = answer,
                known = "fetch https://FAO.org?lang=en: 2.3 million tonnes"
            )
        )
    }

    @Test
    fun `an answer from the notes alone is not asked for links, and one from a page is`() {
        val asked = listOf(
            "read \"Greenhouse\": status active",
            "fetch https://en.wikipedia.org/wiki/Lisbon: 545,000"
        ).map { found ->
            FoundAnswerPrompt
                .answer(
                    task = "what is it?",
                    found = listOf(found)
                )
                .first()
                .text
        }

        assertEquals(
            expected = listOf(false, true, true, false),
            actual = listOf(
                "[site](https://…)" in asked[0],
                "never as a link: no web page was read" in asked[0],
                "[site](https://…)" in asked[1],
                "never as a link" in asked[1]
            )
        )
    }
}
