package com.testbuddy.app

import com.testbuddy.Suite
import com.testbuddy.SuiteRunner
import java.io.PrintStream
import java.nio.file.Path

/**
 * The automatic run, in the process the manager starts with [AppProcess.RUNNER_FLAG]: every automated case of [suite]
 * runs on fresh copies in [workFolder], away from the tester's app, and each start, step and end goes to [out] as a
 * [RunnerEvent] line, which the manager reads as they come.
 */
internal fun runAutomatedCases(
    suite: Suite,
    workFolder: Path,
    out: PrintStream = System.out
) {
    fun send(event: RunnerEvent) = synchronized(out) {
        out.println(event.line())
        out.flush()
    }
    SuiteRunner(suite = suite, workFolder = workFolder).run(
        onStart = { send(RunnerEvent.Started(id = it.id)) },
        onStep = { send(RunnerEvent.Step(text = it)) },
        onResult = { send(RunnerEvent.Ended(result = it)) }
    )
    send(RunnerEvent.Done)
}
