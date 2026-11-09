package app.notekea

import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.resources.Res
import app.notekea.resources.crash_close
import app.notekea.resources.crash_copied
import app.notekea.resources.crash_copy
import app.notekea.resources.crash_message
import app.notekea.resources.crash_report_github
import app.notekea.resources.crash_report_not_saved
import app.notekea.resources.crash_report_saved
import app.notekea.resources.crash_title
import co.touchlab.kermit.Logger
import com.testbuddy.crash.CrashBuddy
import com.testbuddy.crash.CrashConfig
import com.testbuddy.crash.CrashTexts
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.koin.core.Koin
import java.nio.file.Path

private const val CRASH_FOLDER = "crashes"
private const val CRASH_LOG_TAG = "CrashBuddy"
private const val CRASH_MESSAGE = "Unexpected error, kept as a crash report"
private const val ISSUE_FORM =
    "https://github.com/juanchurtado1991/notekea/issues/new?template=crash_report.yml"

/** Notekea's crash reports: in the app data folder's `crashes`, with Notekea's texts and its GitHub form. */
internal fun crashBuddyOf(
    koin: Koin,
    appName: String
): CrashBuddy {
    val conventions = koin.get<PlatformConventions>()
    val logger = Logger.withTag(tag = CRASH_LOG_TAG)

    return CrashBuddy(
        config = CrashConfig(
            appName = appName,
            version = koin.get<SystemEnvironment>().appVersion,
            reportFolder = Path.of(conventions.appDataDirectory, CRASH_FOLDER),
            issueFormUrl = ISSUE_FORM,
            texts = runBlocking { crashTexts() },
            isCloseLast = conventions.dialogButtonOrder == DialogButtonOrder.CANCEL_THEN_PRIMARY
        ),
        onError = {
            logger.e(throwable = it) { CRASH_MESSAGE }
        }
    )
}

private suspend fun crashTexts(): CrashTexts = CrashTexts(
    title = getString(resource = Res.string.crash_title),
    message = getString(resource = Res.string.crash_message),
    saved = getString(resource = Res.string.crash_report_saved),
    notSaved = getString(resource = Res.string.crash_report_not_saved),
    copy = getString(resource = Res.string.crash_copy),
    copied = getString(resource = Res.string.crash_copied),
    report = getString(resource = Res.string.crash_report_github),
    close = getString(resource = Res.string.crash_close)
)
