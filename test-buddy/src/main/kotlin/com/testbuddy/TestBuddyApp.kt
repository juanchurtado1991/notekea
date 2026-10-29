package com.testbuddy

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.testbuddy.app.SelfTestContent
import com.testbuddy.app.SelfTestFolders
import com.testbuddy.app.SelfTestSession
import com.testbuddy.app.SelfTestTheme
import com.testbuddy.app.sessionActions
import kotlinx.coroutines.Dispatchers
import java.nio.file.Files

/**
 * Opens the "<app> Self-Test" window for [suite]: the host's `main` is `fun main() = testBuddyApp(suite)`. What the
 * tester does is kept in [folders], `<Documents>/<app> Self-Test` unless given.
 */
public fun testBuddyApp(
    suite: Suite,
    folders: SelfTestFolders = SelfTestFolders.default(appName = suite.appName)
): Unit = application {
    val scope = rememberCoroutineScope()
    val session =
        remember { SelfTestSession(suite = suite, folders = folders, scope = scope, background = Dispatchers.IO) }
    val actions = remember {
        val installed = suite.installedApp(HostSystem.current())?.takeIf { Files.exists(it) }
        sessionActions(session = session, folders = folders, installedApp = installed)
    }
    val environment = remember { ReportEnvironment.current() }
    val state by session.state.collectAsState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "${suite.appName} Self-Test",
        state = rememberWindowState(size = DpSize(width = 860.dp, height = 820.dp))
    ) {
        SelfTestTheme { SelfTestContent(suite = suite, environment = environment, state = state, session = actions) }
    }
}
