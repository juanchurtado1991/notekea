package com.testbuddy.app

/** The window's actions for [session], with the desktop doing what the session cannot ([folders] to open). */
internal fun sessionActions(
    session: SelfTestSession,
    folders: SelfTestFolders
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
        onPickScreenshot = DesktopActions::pickPicture
    ),
    report = ReportActions(
        onCopy = DesktopActions::copy,
        onOpenReport = { DesktopActions.open(folder = folders.report) },
        onBack = session::backToManual,
        onFinish = session::finish
    )
)
