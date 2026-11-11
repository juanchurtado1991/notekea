package app.notekea

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.use
import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.FakeAppearanceStore
import app.notekea.core.platform.domain.FakeSystemEnvironment
import app.notekea.core.platform.domain.SystemAccent
import app.notekea.core.platform.domain.SystemAppearance
import app.notekea.core.platform.domain.SystemMotion
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.theme.LocalAppearance
import app.notekea.core.presentation.designsystem.theme.LocalSystemDarkTheme
import app.notekea.core.presentation.designsystem.theme.LocalSystemReduceMotion
import app.notekea.core.presentation.platform.AppEnvironment
import app.notekea.core.presentation.platform.LocalAppVersion
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.presentation.platform.LocalResizePointerIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** What every window reads comes from one place, which the Auto-Test's cases share with the app (audit 64). */
class NotekeaAppLocalsTest {
    private val appearance = Appearance(theme = ColorTheme.SAGE)
    private val environment = AppEnvironment(
        systemAppearance = object : SystemAppearance {
            override fun observeDarkTheme(): Flow<Boolean?> = flowOf(value = true)
        },
        systemAccent = object : SystemAccent {
            override suspend fun read(): Long? = null
        },
        systemMotion = object : SystemMotion {
            override suspend fun isReduced(): Boolean = true
        },
        systemEnvironment = FakeSystemEnvironment(
            operatingSystemName = "Linux",
            userHome = "/home/ana"
        ),
        appearanceStore = FakeAppearanceStore(initial = appearance),
        preferencesStore = FakeAppPreferencesStore()
    )

    @Test
    fun `the windows read the system, the kept appearance, the version and the desktop's parts`() {
        var seen: List<Any?> = emptyList()
        var cursor: PointerIcon = PointerIcon.Hand
        var fileDrop: Modifier = Modifier

        render {
            seen = listOf(
                LocalSystemDarkTheme.current,
                LocalSystemReduceMotion.current,
                LocalAppearance.current,
                LocalAppVersion.current
            )

            cursor = LocalResizePointerIcon.current
            fileDrop = LocalFileDrop.current {}
        }

        assertEquals(
            expected = listOf(true, true, appearance, "1.0.0"),
            actual = seen
        )

        assertNotEquals(
            illegal = PointerIcon.Hand,
            actual = cursor
        )

        assertNotEquals(
            illegal = Modifier,
            actual = fileDrop
        )
    }

    private fun render(content: @Composable () -> Unit) {
        UiThread.run {
            ImageComposeScene(
                width = SIZE,
                height = SIZE
            ) {
                NotekeaAppLocals(
                    environment = environment,
                    initialAppearance = Appearance(),
                    content = content
                )
            }.use { scene ->
                repeat(times = FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
            }
        }
    }

    private companion object {
        const val FRAMES = 3
        const val FRAME_NANOS = 16_000_000L
        const val SIZE = 100
    }
}
