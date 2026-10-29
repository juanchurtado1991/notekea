package com.testbuddy.app

import com.testbuddy.copyKeepingTimes
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.deleteRecursively

/**
 * Backs up the installed app's own folders ([paths]) into [folder] before the manual cases change them, and puts them
 * back as they were: a folder that did not exist then is deleted. The backup is made once, so a Self-Test closed
 * and opened again never backs up what the tests changed.
 */
@OptIn(ExperimentalPathApi::class)
internal class AppBackup(
    private val folder: Path,
    private val paths: () -> List<Path>
) {
    private val manifest = folder.resolve(MANIFEST)

    val isMade: Boolean get() = Files.isRegularFile(manifest)

    fun make() {
        if (isMade) return
        Files.createDirectories(folder)
        val lines = paths().mapIndexed { index, path ->
            val exists = Files.exists(path)
            if (exists) {
                path.copyToRecursively(
                    target = slot(index = index),
                    followLinks = false,
                    copyAction = copyKeepingTimes()
                )
            }
            "${if (exists) KEPT else ABSENT}\t$path"
        }
        Files.write(manifest, lines)
    }

    fun restore() {
        if (!isMade) return
        Files.readAllLines(manifest).forEachIndexed { index, line ->
            val (state, location) = line.split("\t", limit = 2)
            val path = Path.of(location)
            path.deleteRecursively()
            if (state ==
                KEPT
            ) {
                slot(index = index).copyToRecursively(
                    target = path,
                    followLinks = false,
                    copyAction = copyKeepingTimes()
                )
            }
        }
        folder.deleteRecursively()
    }

    private fun slot(index: Int): Path = folder.resolve(index.toString())

    private companion object {
        const val MANIFEST = "backup.txt"
        const val KEPT = "kept"
        const val ABSENT = "absent"
    }
}
