package com.testbuddy.app

import java.nio.file.Path

/**
 * The window's actions for [session], with the desktop doing what the session cannot: [folders] to open, and the
 * [installedApp] to start, when it is installed.
 */
internal fun sessionActions(
    session: SelfTestSession,
    folders: SelfTestFolders,
    installedApp: Path?
): SelfTestActions = SelfTestActions(
    onStart = session::startAutomatic,
    onStop = session::stopAutomatic,
    onNext = session::goToManual,
    manual = ManualActions(
        onPass = session::pass,
        onFail = session::fail,
        onSkip = session::skip,
        onPrevious = session::previous,
        onOpenData = { DesktopActions.open(folder = folders.data) },
        onPickScreenshot = DesktopActions::pickPicture,
        onOpenApp = installedApp?.let { path ->
            { case ->
                val output = folders.report.resolve("${case.id} output.txt")
                DesktopActions.launch(path = path, environment = case.launchEnvironment, output = output)
            }
        }
    ),
    report = ReportActions(
        onCopy = DesktopActions::copy,
        onOpenReport = { DesktopActions.open(folder = folders.report) },
        onBack = session::backToManual,
        onFinish = session::finish
    )
)
