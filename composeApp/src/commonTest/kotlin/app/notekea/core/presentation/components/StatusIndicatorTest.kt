package app.notekea.core.presentation.components

import app.notekea.core.presentation.theme.WorkspaceBlueSchemes
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusIndicatorTest {
    @Test
    fun `each state maps to its semantic role in both themes`() {
        listOf(WorkspaceBlueSchemes.light, WorkspaceBlueSchemes.dark).forEach { scheme ->
            assertEquals(
                expected = listOf(scheme.primary, scheme.error, scheme.onSurfaceVariant),
                actual = StatusIndicator.entries.map { it.color(colorScheme = scheme) }
            )
        }
    }
}
