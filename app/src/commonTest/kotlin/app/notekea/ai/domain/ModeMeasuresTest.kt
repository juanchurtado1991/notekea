package app.notekea.ai.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** Where Auto may hand the agent what the plan could not do, by what it was measured at against the size's target. */
class ModeMeasuresTest {
    // The developer, 2026-11-26: Auto never picks a mode not measured to the size's target (65, 80 and 90 %).
    @Test
    fun `auto hands the agent work only on the sizes measured to their target`() {
        assertEquals(
            expected = listOf(false, false, false),
            actual = ModelSize.entries.map { ModeMeasures.isAgentPicked(size = it) }
        )
    }
}
