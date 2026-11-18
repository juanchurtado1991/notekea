package app.notekea.chat.eval

import kotlin.test.Test
import kotlin.test.assertEquals

/** The fair check of `docs/loop-goal-plan.md`, step 1.3: what a right answer worded otherwise still holds. */
class FairTextTest {
    private fun held(
        wanted: String,
        answer: String
    ) = FairText.isHeld(
        wanted = wanted,
        answer = FairText.plain(text = answer)
    )

    @Test
    fun `emphasis, commas and spaces do not hide a figure`() {
        assertEquals(
            expected = listOf(true, true, true),
            actual = listOf(
                held(
                    wanted = "38 días",
                    answer = "Faltan **38** días."
                ),
                held(
                    wanted = "1,350",
                    answer = "`1350` euros"
                ),
                held(
                    wanted = "Rosa Quesada",
                    answer = "Rosa\n  Quesada"
                )
            )
        )
    }

    @Test
    fun `a day is held by its words in English or Spanish, and another day is not`() {
        assertEquals(
            expected = listOf(true, true, true, false, false),
            actual = listOf(
                held(
                    wanted = "2026-12-06",
                    answer = "on 6 December 2026"
                ),
                held(
                    wanted = "2026-12-06",
                    answer = "on December 6"
                ),
                held(
                    wanted = "2026-12-06",
                    answer = "el 6 de diciembre"
                ),
                held(
                    wanted = "2026-12-06",
                    answer = "on 16 December"
                ),
                held(
                    wanted = "2026-12-01",
                    answer = "on December 16"
                )
            )
        )
    }
}
