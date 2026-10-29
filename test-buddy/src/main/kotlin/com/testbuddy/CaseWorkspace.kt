package com.testbuddy

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.deleteRecursively

/** Makes each case's [CaseFolders] under [workFolder], with a fresh copy of [dataSources], and deletes them after. */
@OptIn(ExperimentalPathApi::class)
internal class CaseWorkspace(
    private val workFolder: Path,
    private val dataSources: List<Path>
) {
    fun prepare(caseId: String): CaseFolders {
        val root = caseRoot(caseId = caseId)
        root.deleteRecursively()
        val folders = CaseFolders(
            appData = Files.createDirectories(root.resolve(APP_DATA)),
            data = Files.createDirectories(root.resolve(DATA))
        )
        dataSources.forEach {
            it.copyToRecursively(target = folders.data, followLinks = false, copyAction = copyKeepingTimes())
        }
        return folders
    }

    /**
     * Deletes the case's folders. An app may still be finishing work there (a database closing), so what cannot be
     * deleted now is deleted when the process ends.
     */
    fun clean(caseId: String) {
        val root = caseRoot(caseId = caseId)
        val isDeleted = runCatching { root.deleteRecursively() }.isSuccess
        if (!isDeleted) Runtime.getRuntime().addShutdownHook(Thread { runCatching { root.deleteRecursively() } })
    }

    fun screenshotFile(caseId: String): Path =
        Files.createDirectories(workFolder.resolve(SCREENSHOTS)).resolve("${fileName(caseId = caseId)}.png")

    private fun caseRoot(caseId: String): Path = workFolder.resolve(CASES).resolve(fileName(caseId = caseId))

    private fun fileName(caseId: String): String = caseId.replace(Regex(pattern = "[^A-Za-z0-9._-]"), "_")

    private companion object {
        const val CASES = "cases"
        const val APP_DATA = "app-data"
        const val DATA = "data"
        const val SCREENSHOTS = "screenshots"
    }
}
