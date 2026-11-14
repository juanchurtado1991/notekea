package app.notekea.core.preferences.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.preferences.domain.AppLanguage
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.preferences.domain.LineWidth
import app.notekea.core.preferences.domain.WebUpdateEvery
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * [AppPreferencesStore] on Multiplatform Settings, `app.*` keys beside the appearance's; read on the first
 * [observe] and then held, so every window sees each [save] at once.
 */
class SettingsAppPreferencesStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : AppPreferencesStore {
    private val current = MutableStateFlow<AppPreferences?>(value = null)

    override fun observe(): Flow<AppPreferences> = flow {
        if (current.value == null) {
            current.compareAndSet(
                expect = null,
                update = read()
            )
        }

        emitAll(current.filterNotNull())
    }

    override suspend fun save(preferences: AppPreferences) = saving.withLock {
        withContext(context = dispatchers.io) {
            settings.putString(
                key = LANGUAGE_KEY,
                value = preferences.language.id
            )

            settings.putBoolean(
                key = OPENS_LAST_VAULT_KEY,
                value = preferences.opensLastVault
            )

            settings.putInt(
                key = TEXT_SIZE_KEY,
                value = preferences.textSize
            )

            settings.putString(
                key = LINE_WIDTH_KEY,
                value = preferences.lineWidth.id
            )

            settings.putBoolean(
                key = SEEN_WELCOME_KEY,
                value = preferences.hasSeenWelcome
            )

            settings.putString(
                key = WEB_UPDATE_KEY,
                value = preferences.webUpdate.id
            )
        }

        current.value = preferences
    }

    // One save at a time, in the order asked: two quick changes could end with the older one kept.
    private val saving = Mutex()

    private suspend fun read(): AppPreferences = withContext(context = dispatchers.io) {
        AppPreferences(
            language = AppLanguage.fromId(id = settings.getStringOrNull(key = LANGUAGE_KEY)),
            opensLastVault = settings.getBoolean(
                key = OPENS_LAST_VAULT_KEY,
                defaultValue = true
            ),
            textSize = settings.getInt(
                key = TEXT_SIZE_KEY,
                defaultValue = AppPreferences.DEFAULT_TEXT_SIZE
            ),
            lineWidth = LineWidth.fromId(id = settings.getStringOrNull(key = LINE_WIDTH_KEY)),
            hasSeenWelcome = settings.getBoolean(
                key = SEEN_WELCOME_KEY,
                defaultValue = false
            ),
            webUpdate = WebUpdateEvery.fromId(id = settings.getStringOrNull(key = WEB_UPDATE_KEY))
        )
    }

    private companion object {
        const val LANGUAGE_KEY = "app.language"
        const val OPENS_LAST_VAULT_KEY = "app.opens_last_vault"
        const val TEXT_SIZE_KEY = "app.editor_text_size"
        const val LINE_WIDTH_KEY = "app.editor_line_width"
        const val SEEN_WELCOME_KEY = "app.has_seen_welcome"
        const val WEB_UPDATE_KEY = "app.web_update"
    }
}
