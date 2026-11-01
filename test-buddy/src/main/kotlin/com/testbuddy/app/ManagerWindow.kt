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
import java.awt.GraphicsEnvironment
import kotlin.system.exitProcess

/**
 * The manager: a small window of its own next to the app, always on top, at the bottom left of the screen, which the
 * tester moves and minimizes as any window; closing it only minimizes it, so it is never lost. It is never taller than
 * the screen leaves room for, and its tabs scroll in it ([TabLayout]).
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
        size = DpSize(width = WIDTH_DP.dp, height = heightOnScreen().dp)
    )
    val machine = remember { AppEnvironment.current().let { "${it.system.substringBefore(" (")} · ${it.screen}" } }
    val actions = remember { managerActions(session = session, folders = folders) }
    Window(
        onCloseRequest = { windowState.isMinimized = true },
        state = windowState,
        title = "${suite.appName} QA",
        alwaysOnTop = true
    ) {
        SelfTestTheme {
            ManagerContent(
                suite = suite,
                machine = machine,
                dataFolder = folders.data,
                state = state,
                actions = actions
            )
        }
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
    onRestart = {
        AppProcess.start(arguments = emptyList(), environment = session.launchEnvironment())
        exitProcess(status = 0)
    },
    onPickScreenshot = DesktopActions::pickPicture,
    onCopy = DesktopActions::copy,
    onOpenReport = { DesktopActions.open(folder = folders.report) }
)

/** [HEIGHT_DP], or less on a short screen: the room the taskbar or the Dock leaves, with a margin. */
private fun heightOnScreen(): Int {
    val room = if (GraphicsEnvironment.isHeadless()) {
        HEIGHT_DP
    } else {
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .maximumWindowBounds.height - SCREEN_MARGIN_DP
    }
    return room.coerceIn(minimumValue = MIN_HEIGHT_DP, maximumValue = HEIGHT_DP)
}

private const val WIDTH_DP = 380
private const val HEIGHT_DP = 640
private const val MIN_HEIGHT_DP = 360
private const val SCREEN_MARGIN_DP = 40
