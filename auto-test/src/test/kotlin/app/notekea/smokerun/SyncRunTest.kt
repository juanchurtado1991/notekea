package app.notekea.smokerun

import androidx.compose.ui.test.ExperimentalTestApi
import app.notekea.core.domain.Outcome
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus
import org.eclipse.jgit.api.Git
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The sync run of 4.3: the Sample Vault and the Ghost Serializer vault (371 notes) connected to a repository on disk,
 * their first sync timed, a note changed every two seconds for 20 s as typing saves it (auto-commit), a Sync now with
 * one change, then the app idle with sync on; memory sampled after each.
 */
@OptIn(ExperimentalTestApi::class)
class SyncRunTest {
    @Test
    fun `syncing a vault takes little time and memory, and idle sync costs nothing`() = smokeRun(
        name = RUN,
        vaults = listOf(SAMPLE_VAULT, GHOST)
    ) { app ->
        openVault(
            app = app,
            name = SAMPLE_VAULT,
            shownNote = HOME
        )

        SmokeRunReport.sample(
            run = RUN,
            moment = "sample-open"
        )

        val sample = app.syncOf(name = SAMPLE_VAULT)

        val first = sample.connect(
            settings = SyncSettings(remoteUrl = remote(name = SAMPLE_VAULT)),
            token = null
        )

        SmokeRunReport.sample(
            run = RUN,
            moment = "sample-first-sync"
        )

        val note = File(app.vaultPath(name = SAMPLE_VAULT), "Home.md")

        // Real time: the test's coroutines run on virtual time, and the engine's 10 s wait is real.
        repeat(times = TYPING_SAVES) { save ->
            note.appendText("\nA line typed at save $save.\n")
            Thread.sleep(SAVE_EVERY_MILLIS)
        }

        awaitStatus(sync = sample) {
            it == SyncStatus.UpToDate
        }

        SmokeRunReport.sample(
            run = RUN,
            moment = "sample-typing-committed"
        )

        note.appendText("\nOne more change.\n")
        sample.syncNow()

        SmokeRunReport.sample(
            run = RUN,
            moment = "sample-sync-now"
        )

        val ghost = app.syncOf(name = GHOST)

        val ghostFirst = ghost.connect(
            settings = SyncSettings(remoteUrl = remote(name = GHOST)),
            token = null
        )

        SmokeRunReport.sample(
            run = RUN,
            moment = "ghost-first-sync"
        )

        val idle = SmokeRunReport.idleSeconds

        if (idle > 0) {
            Thread.sleep(idle * MILLIS)

            SmokeRunReport.sample(
                run = RUN,
                moment = "idle-${idle}s-sync-on"
            )
        }

        assertEquals(
            expected = listOf<Any?>(
                Outcome.Success(value = Unit),
                Outcome.Success(value = Unit),
                SyncStatus.UpToDate
            ),
            actual = listOf(first, ghostFirst, sample.status.value)
        )
    }

    private fun awaitStatus(
        sync: app.notekea.sync.domain.VaultSync,
        isReached: (SyncStatus) -> Boolean
    ) {
        repeat(times = STATUS_TRIES) {
            if (isReached(sync.status.value)) return
            Thread.sleep(STATUS_EVERY_MILLIS)
        }
    }

    private fun remote(name: String): String {
        val folder = Files
            .createTempDirectory("notekea-sync-run")
            .resolve("$name.git")
            .toFile()

        Git
            .init()
            .setBare(true)
            .setDirectory(folder)
            .setInitialBranch("main")
            .call()
            .close()

        return folder.toURI().toString()
    }

    private companion object {
        const val RUN = "sync"
        const val HOME = "Home"
        const val GHOST = "Ghost Serializer"
        const val TYPING_SAVES = 10
        const val SAVE_EVERY_MILLIS = 2_000L
        const val STATUS_EVERY_MILLIS = 250L
        const val STATUS_TRIES = 120
        const val MILLIS = 1_000L
    }
}
