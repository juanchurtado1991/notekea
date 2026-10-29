package com.testbuddy

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.CopyActionContext
import kotlin.io.path.CopyActionResult
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory

/**
 * A copy action for `copyToRecursively` that replaces what is there and keeps each file's dates, so a case that sorts
 * notes by date modified sees the dates the test data was prepared with.
 */
@OptIn(ExperimentalPathApi::class)
internal fun copyKeepingTimes(): CopyActionContext.(
    source: Path,
    target: Path
) -> CopyActionResult = { source, target ->
    if (source.isDirectory()) {
        Files.createDirectories(target)
    } else {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
    }
    CopyActionResult.CONTINUE
}
