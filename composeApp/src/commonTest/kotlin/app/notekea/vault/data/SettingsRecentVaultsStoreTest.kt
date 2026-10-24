package app.notekea.vault.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.vault.domain.RecentVault
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SettingsRecentVaultsStoreTest {
    private val testDispatcher = StandardTestDispatcher()
    private val settings = MapSettings()
    private val store =
        SettingsRecentVaultsStore(settings = settings, dispatchers = FakeAppDispatchers(dispatcher = testDispatcher))

    @Test
    fun `recent vaults round-trip in order`() = runTest(context = testDispatcher) {
        val recentVaults = listOf(recent(path = NOTES, millis = LATER), recent(path = WORK, millis = EARLIER))

        store.write(recentVaults = recentVaults)

        assertEquals(
            expected = recentVaults,
            actual = store.read()
        )
    }

    @Test
    fun `a shorter list removes the entries it no longer has`() = runTest(context = testDispatcher) {
        store.write(recentVaults = listOf(recent(path = NOTES, millis = LATER), recent(path = WORK, millis = EARLIER)))

        store.write(recentVaults = listOf(recent(path = WORK, millis = LATER)))

        assertEquals(
            expected = setOf("vaults.recent.count", "vaults.recent.0.path", "vaults.recent.0.openedAt"),
            actual = settings.keys
        )
    }

    @Test
    fun `an entry without a path is dropped and one without a time is kept as long ago`() = runTest(
        context = testDispatcher
    ) {
        settings.putInt(key = "vaults.recent.count", value = 2)
        settings.putLong(key = "vaults.recent.0.openedAt", value = LATER)
        settings.putString(key = "vaults.recent.1.path", value = WORK)

        assertEquals(
            expected = listOf(recent(path = WORK, millis = 0L)),
            actual = store.read()
        )
    }

    private fun recent(
        path: String,
        millis: Long
    ): RecentVault = RecentVault(path = path, lastOpenedAt = Instant.fromEpochMilliseconds(epochMilliseconds = millis))

    companion object {
        private const val EARLIER = 1_700_000_000_000L
        private const val LATER = 1_800_000_000_000L
        private const val NOTES = "/home/ana/Notes"
        private const val WORK = "/home/ana/Work"
    }
}
