package app.notekea.core.presentation.platform

import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.vault.presentation.welcome.WelcomePreviewData
import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayPathsTest {
    private val linux = WelcomePreviewData.platformConventions

    @Test
    fun `the home folder is written as a tilde on macOS and Linux`() {
        val macos = linux.copy(operatingSystem = OperatingSystem.MACOS, homeDirectory = "/home/ana/")

        assertEquals(
            expected = listOf("~/Notes", "~", "/home/anabel/Notes", "/srv/Notes", "~/Work"),
            actual = listOf(
                displayPath(path = "/home/ana/Notes", platformConventions = linux),
                displayPath(path = "/home/ana", platformConventions = linux),
                displayPath(path = "/home/anabel/Notes", platformConventions = linux),
                displayPath(path = "/srv/Notes", platformConventions = linux),
                displayPath(path = "/home/ana/Work", platformConventions = macos)
            )
        )
    }

    @Test
    fun `windows paths and an unknown home stay as they are`() {
        val windows = linux.copy(operatingSystem = OperatingSystem.WINDOWS, homeDirectory = "C:\\Users\\ana")

        assertEquals(
            expected = listOf("C:\\Users\\ana\\Notes", "/home/ana/Notes"),
            actual = listOf(
                displayPath(path = "C:\\Users\\ana\\Notes", platformConventions = windows),
                displayPath(path = "/home/ana/Notes", platformConventions = linux.copy(homeDirectory = ""))
            )
        )
    }
}
