package app.notekea.ai.data

import app.notekea.ai.domain.ModelSize
import app.notekea.core.domain.FakeAppDispatchers
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The model in use is kept between runs, and none is kept after its deletion. */
class SettingsActiveModelStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()

    private fun store() = SettingsActiveModelStore(
        settings = settings,
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `no model is in use at first, and a chosen one is kept for the next run`() = runTest(
        context = testDispatcher
    ) {
        val firstRun = store().observe().first()
        store().save(size = ModelSize.MEDIUM)

        assertEquals(
            expected = listOf(null, ModelSize.MEDIUM),
            actual = listOf(firstRun, store().observe().first())
        )
    }

    @Test
    fun `clearing it, or a size this version does not know, keeps none`() = runTest(
        context = testDispatcher
    ) {
        store().save(size = ModelSize.LARGE)
        store().save(size = null)
        val cleared = store().observe().first()

        settings.putString(
            key = "models.active",
            value = "huge"
        )

        assertEquals(
            expected = listOf(null, null),
            actual = listOf(cleared, store().observe().first())
        )
    }
}
