package app.notekea.autotest

import app.notekea.autotest.cases.freeUseCase
import app.notekea.autotest.cases.generalCases
import app.notekea.autotest.cases.m1.attachmentCases
import app.notekea.autotest.cases.m1.bookmarkCases
import app.notekea.autotest.cases.m1.dailyCases
import app.notekea.autotest.cases.m1.editorCases
import app.notekea.autotest.cases.m1.editorTableCases
import app.notekea.autotest.cases.m1.editorViewCases
import app.notekea.autotest.cases.m1.embedCases
import app.notekea.autotest.cases.m1.formattingCases
import app.notekea.autotest.cases.m1.memoryCases
import app.notekea.autotest.cases.m1.outlineCases
import app.notekea.autotest.cases.m1.paneCases
import app.notekea.autotest.cases.m1.platformCases
import app.notekea.autotest.cases.m1.propertyCases
import app.notekea.autotest.cases.m1.quickSwitcherCases
import app.notekea.autotest.cases.m1.searchCases
import app.notekea.autotest.cases.m1.tabCases
import app.notekea.autotest.cases.m1.taskCases
import app.notekea.autotest.cases.m1.templateCases
import app.notekea.autotest.cases.m1.themeCases
import app.notekea.autotest.cases.m1.treeCases
import app.notekea.autotest.cases.m1.treeChangeCases
import app.notekea.autotest.cases.m1.vaultCases
import app.notekea.autotest.cases.m1.workspaceLookCases
import app.notekea.autotest.cases.m2.appearanceCases
import app.notekea.autotest.cases.m2.backlinksCases
import app.notekea.autotest.cases.m2.graphCases
import app.notekea.autotest.cases.m2.hybridSearchCases
import app.notekea.autotest.cases.m2.indexCases
import app.notekea.autotest.cases.m2.queryCases
import app.notekea.autotest.cases.m2.relatedCases
import app.notekea.autotest.cases.m2.vaultTaskCases
import app.notekea.autotest.cases.m3.chatCases
import app.notekea.autotest.cases.m3.modelManagerCases
import com.testbuddy.Suite
import com.testbuddy.testBuddy
import java.nio.file.Files

/** Notekea's Auto-Test suite (`qa/AUTO_TEST.md`): every smoke case of `MVP_SMOKE_TEST.md` written so far. */
object NotekeaSuite {
    private const val APP_NAME = "Notekea"
    private const val VERSION_PROPERTY = "notekea.version"
    private const val DEVELOPMENT_VERSION = "development"
    private const val WINDOW_WIDTH = 1280
    private const val WINDOW_HEIGHT = 800
    private const val LOG_LINES = 50

    val suite: Suite by lazy {
        testBuddy(
            appName = APP_NAME,
            version = System.getProperty(VERSION_PROPERTY) ?: DEVELOPMENT_VERSION
        ) {
            launch { folders -> NotekeaCase(folders = folders) }

            data(from = SmokeData.folder)

            window(
                width = WINDOW_WIDTH,
                height = WINDOW_HEIGHT
            )

            diagnostics { recentLog() }

            generalCases()
            platformCases()
            themeCases()
            vaultCases()
            treeCases()
            treeChangeCases()
            editorCases()
            editorViewCases()
            editorTableCases()
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
            appearanceCases()
            indexCases()
            relatedCases()
            backlinksCases()
            hybridSearchCases()
            graphCases()
            queryCases()
            vaultTaskCases()
            modelManagerCases()
            chatCases()
            // Free use comes last, after every other manual case.
            freeUseCase()
        }
    }

    // The last lines of Notekea QA's log (logs/notekea.log, as About's Copy diagnostics reads it).
    private fun recentLog(): String? {
        val log = QaAppData
            .current()
            .resolve("logs")
            .resolve("notekea.log")

        if (!Files.isRegularFile(log)) return null

        return "Notekea's recent log:\n" +
            Files
                .readAllLines(log)
                .takeLast(n = LOG_LINES)
                .joinToString(separator = "\n")
    }
}
