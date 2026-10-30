package com.testbuddy.app

import java.nio.file.Files
import java.nio.file.Path

/**
 * The tester's folder of a QA build, `<Documents>/<app> QA`: [data] holds the copies manual cases use, [report] the
 * report with the screenshots and the app's output, and [work] what Test Buddy keeps for itself (runs, answers).
 */
public class SelfTestFolders(public val root: Path) {
    public val data: Path = root.resolve("Test data")
    public val report: Path = root.resolve("Report")
    internal val work: Path = root.resolve(".self-test")

    public fun create(): SelfTestFolders = apply {
        listOf(data, report, work).forEach { Files.createDirectories(it) }
    }

    public companion object {
        /** `<app> QA` in the user's Documents folder, or in their home folder when there is no Documents. */
        public fun default(appName: String): SelfTestFolders {
            val home = Path.of(System.getProperty("user.home"))
            val documents = home.resolve("Documents")
            val parent = if (Files.isDirectory(documents)) documents else home
            return SelfTestFolders(root = parent.resolve("$appName QA"))
        }
    }
}
