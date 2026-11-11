package app.notekea.autotest

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.data.PropertiesFileSettingsFactory
import app.notekea.core.preferences.data.SettingsAppPreferencesStore
import app.notekea.core.preferences.domain.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path

/**
 * Notekea QA starts in Spanish the first time on a data folder, the language its manual cases name the screens in
 * (2026-11-11); after that it keeps what the tester picks in Settings → General.
 */
object QaLanguage {
    fun startInSpanishOnce(folder: Path) {
        val marker = folder.resolve(MARKER)
        if (Files.exists(marker)) return

        Files.createDirectories(folder)

        val store = SettingsAppPreferencesStore(
            settings = PropertiesFileSettingsFactory(appDataDirectory = folder.toString()).create(),
            dispatchers = JvmAppDispatchers()
        )

        runBlocking {
            store.save(
                preferences = store
                    .observe()
                    .first()
                    .copy(language = AppLanguage.SPANISH)
            )
        }

        Files.createFile(marker)
    }

    private const val MARKER = "qa-language-set"
}
