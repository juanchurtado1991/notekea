package com.testbuddy.app

import com.testbuddy.copyKeepingTimes
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.deleteRecursively

/** Puts fresh copies of a manual case's data folders, found in [sources], into [target], where the tester opens them. */
@OptIn(ExperimentalPathApi::class)
internal class ManualData(
    private val sources: List<Path>,
    private val target: Path
) {
    fun prepare(folders: List<String>) {
        folders.forEach { name ->
            val source = sources.map { it.resolve(name) }.firstOrNull { Files.isDirectory(it) }
                ?: error("No test data folder \"$name\"")
            val copy = target.resolve(name)
            copy.deleteRecursively()
            source.copyToRecursively(target = copy, followLinks = false, copyAction = copyKeepingTimes())
        }
    }

    fun clear() {
        target.deleteRecursively()
    }
}
