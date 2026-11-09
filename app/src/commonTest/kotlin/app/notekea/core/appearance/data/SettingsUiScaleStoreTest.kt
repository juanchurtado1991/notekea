package app.notekea.core.appearance.data

import app.notekea.core.appearance.domain.GetUiScaleUseCase
import app.notekea.core.appearance.domain.SaveUiScaleUseCase
import app.notekea.core.domain.FakeAppDispatchers
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsUiScaleStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()

    private fun store() = SettingsUiScaleStore(
        settings = settings,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `the app starts at its own size, and a zoom is kept for the next run within its limits`() =
        runTest(context = testDispatcher) {
            val firstRun = GetUiScaleUseCase(uiScaleStore = store())()
            SaveUiScaleUseCase(uiScaleStore = store())(scale = 1.3f)
            val nextRun = GetUiScaleUseCase(uiScaleStore = store())()
            SaveUiScaleUseCase(uiScaleStore = store())(scale = 5f)

            assertEquals(
                expected = listOf(1f, 1.3f, 1.6f),
                actual = listOf(firstRun, nextRun, GetUiScaleUseCase(uiScaleStore = store())())
            )
        }
}
