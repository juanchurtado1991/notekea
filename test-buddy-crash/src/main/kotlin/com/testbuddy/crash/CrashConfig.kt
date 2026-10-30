package com.testbuddy.crash

import java.nio.file.Path

/**
 * How the app's crashes are reported: its [appName] and [version], where reports are kept ([reportFolder], the
 * [CrashReports.KEPT] newest), the form that opens with Report on GitHub ([issueFormUrl], none hides the button), the
 * [texts] and whether Close comes last among the buttons ([isCloseLast]: Windows and Linux; macOS puts it first).
 */
public data class CrashConfig(
    val appName: String,
    val version: String,
    val reportFolder: Path,
    val issueFormUrl: String? = null,
    val texts: CrashTexts = CrashTexts.english(appName = appName),
    val isCloseLast: Boolean = true
)
