package app.notekea.selftest

import app.notekea.selftest.cases.m1.attachmentCases
import app.notekea.selftest.cases.m1.editorCases
import app.notekea.selftest.cases.m1.editorViewCases
import app.notekea.selftest.cases.m1.outlineCases
import app.notekea.selftest.cases.m1.platformCases
import app.notekea.selftest.cases.m1.quickSwitcherCases
import app.notekea.selftest.cases.m1.searchCases
import app.notekea.selftest.cases.m1.taskCases
import app.notekea.selftest.cases.m1.themeCases
import app.notekea.selftest.cases.m1.treeCases
import app.notekea.selftest.cases.m1.treeChangeCases
import app.notekea.selftest.cases.m1.vaultCases
import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import com.testbuddy.Suite
import com.testbuddy.testBuddy
import java.nio.file.Path

/** Notekea's Self-Test suite (`qa/SELF_TEST.md`): every smoke case of `MVP_SMOKE_TEST.md` written so far. */
object NotekeaSuite {
    private const val APP_NAME = "Notekea"
    private const val VERSION_PROPERTY = "notekea.version"
    private const val DEVELOPMENT_VERSION = "development"
    private const val WINDOW_WIDTH = 1280
    private const val WINDOW_HEIGHT = 800

    val suite: Suite by lazy {
        testBuddy(appName = APP_NAME, version = System.getProperty(VERSION_PROPERTY) ?: DEVELOPMENT_VERSION) {
            launch { folders -> NotekeaCase(folders = folders) }
            data(from = SmokeData.folder)
            window(width = WINDOW_WIDTH, height = WINDOW_HEIGHT)
            backUp { listOf(installedAppData()) }
            platformCases()
            themeCases()
            vaultCases()
            treeCases()
            treeChangeCases()
            editorCases()
            editorViewCases()
            quickSwitcherCases()
            searchCases()
            taskCases()
            attachmentCases()
            outlineCases()
        }
    }

    // The installed Notekea's data folder (PLATFORM_GUIDELINES.md), which the manual cases change and Finish and
    // clean up puts back.
    private fun installedAppData(): Path =
        Path.of(ResolvePlatformConventionsUseCase(systemEnvironment = JvmSystemEnvironment())().appDataDirectory)
}
