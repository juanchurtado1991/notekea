package app.notekea.core.presentation.components

import app.notekea.core.presentation.theme.NotekeaColorSchemes
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusIndicatorTest {
    @Test
    fun `each state maps to its semantic role in both themes`() {
        listOf(NotekeaColorSchemes.light, NotekeaColorSchemes.dark).forEach { scheme ->
            assertEquals(
                expected = listOf(scheme.primary, scheme.error, scheme.onSurfaceVariant),
                actual = StatusIndicator.entries.map { it.color(colorScheme = scheme) }
            )
        }
    }
}
