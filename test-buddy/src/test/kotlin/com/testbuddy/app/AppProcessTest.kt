package com.testbuddy.app

import com.testbuddy.HostSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/** The automatic run is started so macOS never naps it: a covered window's process ran its cases ten times slower. */
class AppProcessTest {
    @Test
    fun `the automatic run turns App Nap off on macOS and adds nothing elsewhere`() {
        assertEquals(
            expected = listOf(
                listOf(AppProcess.RUNNER_FLAG, "-NSAppSleepDisabled", "YES"),
                listOf(AppProcess.RUNNER_FLAG),
                listOf(AppProcess.RUNNER_FLAG)
            ),
            actual = listOf(HostSystem.MACOS, HostSystem.WINDOWS, HostSystem.LINUX).map {
                AppProcess.runnerArguments(system = it)
            }
        )
    }
}
