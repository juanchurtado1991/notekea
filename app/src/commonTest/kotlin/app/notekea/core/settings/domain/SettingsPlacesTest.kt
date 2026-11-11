package app.notekea.core.settings.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** The setting a question names, in English or Spanish, as Ask about Settings shows it (2026-11-11). */
class SettingsPlacesTest {
    @Test
    fun `a question names its setting by the longest of its words, accents or not`() {
        assertEquals(
            expected = listOf(
                SettingsPlace.SYNC_TOKEN,
                SettingsPlace.APPEARANCE_MODE,
                SettingsPlace.AI_MODEL_SIZE,
                SettingsPlace.SYNC_INTERVAL,
                SettingsPlace.SYNC_AUTO,
                null
            ),
            actual = listOf(
                "¿dónde pongo el token?",
                "how do I turn on dark mode?",
                "¿cómo descargo el modelo?",
                "can it look for changes every minute?",
                "¿se sincroniza automaticamente?",
                "what can you do?"
            ).map { SettingsPlaces.named(text = it) }
        )
    }

    @Test
    fun `a word only part of another does not count`() {
        assertEquals(
            expected = null,
            actual = SettingsPlaces.named(text = "my tokens are fine, thanks")
        )
    }
}
