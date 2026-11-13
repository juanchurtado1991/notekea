package app.notekea.core.appearance.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class UiZoomLevelsTest {
    @Test
    fun `zoom steps by a tenth between its limits, and a stored value is rounded into them`() {
        assertEquals(
            expected = listOf(1.1f, 0.9f, 1.6f, 0.8f, 1.2f, 0.8f),
            actual = listOf(
                UiZoomLevels.larger(scale = 1f),
                UiZoomLevels.smaller(scale = 1f),
                UiZoomLevels.larger(scale = 1.6f),
                UiZoomLevels.smaller(scale = 0.8f),
                UiZoomLevels.clamp(scale = 1.2000001f),
                UiZoomLevels.clamp(scale = 0.2f)
            )
        )
    }
}
