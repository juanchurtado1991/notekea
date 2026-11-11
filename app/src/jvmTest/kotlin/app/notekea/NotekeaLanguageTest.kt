package app.notekea

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.use
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.platform.domain.FakeSystemEnvironment
import app.notekea.core.platform.domain.SystemAccent
import app.notekea.core.platform.domain.SystemAppearance
import app.notekea.core.platform.domain.SystemMotion
import app.notekea.core.preferences.domain.AppLanguage
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.platform.AppEnvironment
import app.notekea.core.presentation.platform.AppLocale
import app.notekea.resources.Res
import app.notekea.resources.settings_language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A language picked in Settings shows at once, everywhere, without losing what the screens remember (5.2). */
class NotekeaLanguageTest {
    private val store =
        FakeAppPreferencesStore(initial = AppPreferences(language = AppLanguage.ENGLISH))
    private val shown = mutableListOf<String>()
    private val remembered = mutableSetOf<Any>()

    @AfterTest
    fun tearDown() {
        AppLocale.apply(language = AppLanguage.SYSTEM)
    }

    @Test
    fun `picking Spanish shows Spanish words at once and keeps what was remembered`() {
        UiThread.run {
            ImageComposeScene(
                width = SIZE,
                height = SIZE
            ) {
                NotekeaAppLocals(
                    environment = environment(),
                    initialAppearance = Appearance(),
                    content = { LanguageLabel() }
                )
            }.use { scene ->
                renderFrames(
                    scene = scene,
                    from = 0
                )

                runBlocking {
                    store.save(preferences = AppPreferences(language = AppLanguage.SPANISH))
                }

                renderFrames(
                    scene = scene,
                    from = FRAMES
                )
            }
        }

        assertEquals(
            expected = listOf("Language", "Idioma", "1"),
            actual = listOf(shown.first(), shown.last(), remembered.size.toString())
        )
    }

    @Composable
    private fun LanguageLabel() {
        val text = stringResource(resource = Res.string.settings_language)
        val kept = remember { Any() }

        SideEffect {
            shown += text
            remembered += kept
        }
    }

    private fun renderFrames(
        scene: ImageComposeScene,
        from: Int
    ) {
        repeat(times = FRAMES) { scene.render(nanoTime = (from + it) * FRAME_NANOS) }
    }

    private fun environment() = AppEnvironment(
        systemAppearance = object : SystemAppearance {
            override fun observeDarkTheme(): Flow<Boolean?> = flowOf(value = false)
        },
        systemAccent = object : SystemAccent {
            override suspend fun read(): Long? = null
        },
        systemMotion = object : SystemMotion {
            override suspend fun isReduced(): Boolean = false
        },
        systemEnvironment = FakeSystemEnvironment(
            operatingSystemName = "Linux",
            userHome = "/home/ana"
        ),
        appearanceStore = FakeAppearanceStore(initial = Appearance()),
        preferencesStore = store
    )

    private companion object {
        const val FRAMES = 4
        const val FRAME_NANOS = 16_000_000L
        const val SIZE = 100
    }
}
