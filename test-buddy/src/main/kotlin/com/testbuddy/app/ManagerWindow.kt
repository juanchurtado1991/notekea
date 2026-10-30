package com.testbuddy.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.testbuddy.Suite
import com.testbuddy.crash.AppEnvironment
import kotlin.system.exitProcess

/**
 * The manager: a small window of its own next to the app, always on top, at the bottom left of the screen, which the
 * tester moves and minimizes as any window; closing it only minimizes it, so it is never lost.
 */
@Composable
internal fun ApplicationScope.ManagerWindow(
    suite: Suite,
    session: ManagerSession,
    folders: SelfTestFolders
) {
    val state by session.state.collectAsState()
    val windowState = rememberWindowState(
        position = WindowPosition(alignment = Alignment.BottomStart),
        size = DpSize(width = WIDTH_DP.dp, height = HEIGHT_DP.dp)
    )
    val machine = remember { AppEnvironment.current().let { "${it.system.substringBefore(" (")} · ${it.screen}" } }
    val actions = remember { managerActions(session = session, folders = folders) }
    Window(
        onCloseRequest = { windowState.isMinimized = true },
        state = windowState,
        title = "${suite.appName} QA",
        alwaysOnTop = true
    ) {
        SelfTestTheme { ManagerContent(suite = suite, machine = machine, state = state, actions = actions) }
    }
}

private fun managerActions(
    session: ManagerSession,
    folders: SelfTestFolders
) = ManagerActions(
    onTab = session::select,
    onStart = session::startAutomatic,
    onStop = session::stopAutomatic,
    onNextStep = session::nextStep,
    onPreviousStep = session::previousStep,
    onPass = session::pass,
    onFail = session::fail,
    onSkip = session::skip,
    onPreviousCase = session::previousCase,
    onOpenData = { DesktopActions.open(folder = folders.data) },
    onRestart = {
        AppProcess.start(arguments = emptyList(), environment = session.launchEnvironment())
        exitProcess(status = 0)
    },
    onPickScreenshot = DesktopActions::pickPicture,
    onCopyReport = DesktopActions::copy,
    onOpenReport = { DesktopActions.open(folder = folders.report) }
)

private const val WIDTH_DP = 380
private const val HEIGHT_DP = 640
