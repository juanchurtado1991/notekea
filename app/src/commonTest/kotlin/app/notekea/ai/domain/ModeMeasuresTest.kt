package app.notekea.ai.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** Which modes Auto may pick on each size, by what they were measured at against the size's target. */
class ModeMeasuresTest {
    // The developer, 2026-11-26: Auto never picks a mode not measured to the size's target (65, 80 and 90 %).
    @Test
    fun `auto picks Deep thinking and the agent only on the sizes measured to their target`() {
        assertEquals(
            expected = listOf(listOf(false, true, false), listOf(false, false, false)),
            actual = listOf(
                ModelSize.entries.map { ModeMeasures.isDeepPicked(size = it) },
                ModelSize.entries.map { ModeMeasures.isAgentPicked(size = it) }
            )
        )
    }
}
