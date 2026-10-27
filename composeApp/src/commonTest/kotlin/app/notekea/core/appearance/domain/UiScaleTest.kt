package app.notekea.core.appearance.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class UiScaleTest {
    @Test
    fun `zoom steps by a tenth between its limits, and a stored value is rounded into them`() {
        assertEquals(
            expected = listOf(1.1f, 0.9f, 1.6f, 0.8f, 1.2f, 0.8f),
            actual = listOf(
                UiScale.larger(scale = 1f),
                UiScale.smaller(scale = 1f),
                UiScale.larger(scale = 1.6f),
                UiScale.smaller(scale = 0.8f),
                UiScale.clamp(scale = 1.2000001f),
                UiScale.clamp(scale = 0.2f)
            )
        )
    }
}
