package app.notekea.core.presentation.platform

import app.notekea.core.preferences.domain.AppLanguage
import java.util.Locale

/**
 * The language Notekea's own words are read in (5.2): the JVM's default locale, which Compose's resources read. The
 * computer's own locale is kept from startup, so [AppLanguage.SYSTEM] goes back to it.
 */
object AppLocale {
    private val system: Locale = Locale.getDefault()

    fun apply(language: AppLanguage) {
        val locale = localeOf(language = language)
        if (Locale.getDefault() != locale) Locale.setDefault(locale)
    }

    private fun localeOf(language: AppLanguage): Locale = when (language) {
        AppLanguage.SYSTEM -> system
        else -> Locale.forLanguageTag(language.id)
    }
}
