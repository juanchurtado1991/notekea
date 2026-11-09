package com.testbuddy.app

import java.nio.file.Path
import kotlin.concurrent.thread

/**
 * One automatic run as the manager sees it: the app started again in its own process with [AppProcess.RUNNER_FLAG],
 * its [RunnerEvent] lines read as they come and passed to [onEvent] (on the reading thread), [onEnd] with its exit code
 * once it stops, and [stop] ends it at once. The app's own output in between goes to [log].
 */
internal class AutomaticRun(
    private val log: Path,
    private val onEvent: (RunnerEvent) -> Unit,
    private val onEnd: (exitCode: Int) -> Unit,
    private val startProcess: () -> Process = {
        AppProcess.start(arguments = listOf(AppProcess.RUNNER_FLAG))
    }
) {
    private var process: Process? = null

    fun start() {
        val started = startProcess()
        process = started

        thread(
            isDaemon = true,
            name = "test-buddy-run"
        ) {
            log
                .toFile()
                .bufferedWriter()
                .use { writer ->
                    started
                        .inputStream
                        .bufferedReader()
                        .forEachLine { line ->
                            val event = RunnerEvent.parse(line = line)
                            if (event == null) writer.appendLine(line) else onEvent(event)
                        }
                }

            onEnd(started.waitFor())
        }
    }

    fun stop() {
        process?.descendants()?.forEach { it.destroy() }

        process?.destroy()
    }
}
