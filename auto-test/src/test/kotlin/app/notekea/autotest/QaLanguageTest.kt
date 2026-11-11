package app.notekea.autotest

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.PropertiesFileSettingsFactory
import app.notekea.core.preferences.data.SettingsAppPreferencesStore
import app.notekea.core.preferences.domain.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

/** Notekea QA speaks Spanish the first time, as its manual cases do, and then keeps the tester's choice. */
class QaLanguageTest {
    private val folder = Files.createTempDirectory("qa-language")

    @Test
    fun `the first start is in Spanish, and a language the tester picks afterwards stays`() {
        QaLanguage.startInSpanishOnce(folder = folder)
        val first = languageIn(folder = folder)

        runBlocking {
            val store = storeIn(folder = folder)

            store.save(
                preferences = store
                    .observe()
                    .first()
                    .copy(language = AppLanguage.ENGLISH)
            )
        }

        QaLanguage.startInSpanishOnce(folder = folder)

        assertEquals(
            expected = listOf(AppLanguage.SPANISH, AppLanguage.ENGLISH),
            actual = listOf(first, languageIn(folder = folder))
        )
    }

    private fun languageIn(folder: Path): AppLanguage = runBlocking {
        storeIn(folder = folder)
            .observe()
            .first()
            .language
    }

    private fun storeIn(folder: Path) = SettingsAppPreferencesStore(
        settings = PropertiesFileSettingsFactory(appDataDirectory = folder.toString()).create(),
        dispatchers = JvmAppDispatchers()
    )
}
