package app.notekea.shell.presentation.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals

/** The first-run welcome waits for the program to finish loading (the developer's smoke of 2026-11-12). */
class OnboardingGateTest {
    @Test
    fun `the welcome waits for the startup and for the opened vault's first scan, then shows`() {
        assertEquals(
            expected = listOf(false, false, false, true, true, false),
            actual = listOf(
                isOpen(isStarting = true),
                isOpen(openedVault = "/vault"),
                isOpen(
                    openedVault = "/vault",
                    loadedVault = "/other"
                ),
                isOpen(
                    openedVault = "/vault",
                    loadedVault = "/vault"
                ),
                isOpen(),
                isOpen(isShown = false)
            )
        )
    }

    private fun isOpen(
        isShown: Boolean = true,
        isStarting: Boolean = false,
        openedVault: String? = null,
        loadedVault: String? = null
    ): Boolean = OnboardingGate.isOpen(
        isShown = isShown,
        isStarting = isStarting,
        openedVault = openedVault,
        loadedVault = loadedVault
    )
}
