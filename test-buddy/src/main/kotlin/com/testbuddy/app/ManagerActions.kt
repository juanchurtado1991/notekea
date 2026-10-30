package com.testbuddy.app

import java.nio.file.Path

/** Everything the manager's buttons do, gathered so its screens stay a picture of the state. */
internal class ManagerActions(
    val onTab: (ManagerTab) -> Unit,
    val onStart: () -> Unit,
    val onStop: () -> Unit,
    val onNextStep: () -> Unit,
    val onPreviousStep: () -> Unit,
    val onPass: () -> Unit,
    val onFail: (note: String, screenshot: Path?) -> Unit,
    val onSkip: () -> Unit,
    val onPreviousCase: () -> Unit,
    val onOpenData: () -> Unit,
    val onRestart: () -> Unit,
    val onPickScreenshot: () -> Path?,
    val onCopyReport: (String) -> Unit,
    val onOpenReport: () -> Unit
)
