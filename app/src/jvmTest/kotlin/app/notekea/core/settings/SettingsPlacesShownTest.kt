package app.notekea.core.settings

import app.notekea.core.settings.domain.SettingsPlace
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Every setting has a place the chat can show, as every Android screen has an intent (`GUIDELINES.md`, Settings): each
 * [SettingsPlace] is spotlit on some screen, so a setting added without one is caught here.
 */
class SettingsPlacesShownTest {
    @Test
    fun `every place is spotlit on a settings screen`() {
        val sources = File(SOURCES)
            .walkTopDown()
            .filter {
                it.isFile && it.extension == "kt"
            }
            .joinToString(separator = "\n") { it.readText() }

        val missing = SettingsPlace.entries.filterNot {
            "serialFor(place = SettingsPlace.${it.name})" in sources
        }

        assertEquals(
            expected = emptyList(),
            actual = missing
        )
    }

    private companion object {
        const val SOURCES = "src/commonMain/kotlin"
    }
}
