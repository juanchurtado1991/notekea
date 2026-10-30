package com.testbuddy.app

import java.nio.file.Path
import kotlin.concurrent.thread

/**
 * One automatic run as the manager sees it: the app started again in its own process with [AppProcess.RUNNER_FLAG],
 * its [RunnerEvent] lines read as they come and passed to [onEvent] (on the reading thread), [onEnd] once it stops,
 * and [stop] ends it at once. The app's own output in between goes to [log].
 */
internal class AutomaticRun(
    private val log: Path,
    private val onEvent: (RunnerEvent) -> Unit,
    private val onEnd: () -> Unit
) {
    private var process: Process? = null

    fun start() {
        val started = AppProcess.start(arguments = listOf(AppProcess.RUNNER_FLAG))
        process = started
        thread(isDaemon = true, name = "test-buddy-run") {
            log.toFile().bufferedWriter().use { writer ->
                started.inputStream.bufferedReader().forEachLine { line ->
                    val event = RunnerEvent.parse(line = line)
                    if (event == null) writer.appendLine(line) else onEvent(event)
                }
            }
            onEnd()
        }
    }

    fun stop() {
        process?.descendants()?.forEach { it.destroy() }
        process?.destroy()
    }
}
