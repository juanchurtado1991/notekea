package app.notekea.selftest

import app.notekea.selftest.cases.freeUseCase
import app.notekea.selftest.cases.generalCases
import app.notekea.selftest.cases.m1.attachmentCases
import app.notekea.selftest.cases.m1.bookmarkCases
import app.notekea.selftest.cases.m1.dailyCases
import app.notekea.selftest.cases.m1.editorCases
import app.notekea.selftest.cases.m1.editorViewCases
import app.notekea.selftest.cases.m1.embedCases
import app.notekea.selftest.cases.m1.formattingCases
import app.notekea.selftest.cases.m1.memoryCases
import app.notekea.selftest.cases.m1.outlineCases
import app.notekea.selftest.cases.m1.paneCases
import app.notekea.selftest.cases.m1.platformCases
import app.notekea.selftest.cases.m1.propertyCases
import app.notekea.selftest.cases.m1.quickSwitcherCases
import app.notekea.selftest.cases.m1.searchCases
import app.notekea.selftest.cases.m1.tabCases
import app.notekea.selftest.cases.m1.taskCases
import app.notekea.selftest.cases.m1.templateCases
import app.notekea.selftest.cases.m1.themeCases
import app.notekea.selftest.cases.m1.treeCases
import app.notekea.selftest.cases.m1.treeChangeCases
import app.notekea.selftest.cases.m1.vaultCases
import app.notekea.selftest.cases.m1.workspaceLookCases
import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import com.testbuddy.HostSystem
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
            installedApp { system -> installedProgram(system = system) }
            generalCases()
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
            dailyCases()
            tabCases()
            paneCases()
            templateCases()
            bookmarkCases()
            propertyCases()
            workspaceLookCases()
            embedCases()
            formattingCases()
            memoryCases()
            // Free use comes last, after every other manual case.
            freeUseCase()
        }
    }

    // Where each system's installer puts Notekea's program (ROADMAP.md fix #68).
    private fun installedProgram(system: HostSystem): Path = when (system) {
        HostSystem.WINDOWS -> Path.of(System.getenv("ProgramFiles") ?: "C:\\Program Files", APP_NAME, "$APP_NAME.exe")
        HostSystem.MACOS -> Path.of("/Applications", "$APP_NAME.app", "Contents", "MacOS", APP_NAME)
        HostSystem.LINUX -> Path.of("/opt", APP_NAME.lowercase(), "bin", APP_NAME)
    }

    // The installed Notekea's data folder (PLATFORM_GUIDELINES.md), which the manual cases change and Finish and
    // clean up puts back.
    private fun installedAppData(): Path =
        Path.of(ResolvePlatformConventionsUseCase(systemEnvironment = JvmSystemEnvironment())().appDataDirectory)
}
