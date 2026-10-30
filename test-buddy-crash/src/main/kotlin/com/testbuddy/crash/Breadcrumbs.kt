package com.testbuddy.crash

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * The last steps a person took in the app, newest last, so a crash report or a test's failure says what led to it: the
 * app records a short name for each action ([record]), never what the person wrote. The same step again right after
 * itself (typing, scrolling) is counted on one line, `10:15:02 Editor: OnTextChange (×37)`; only the [MAX_STEPS]
 * newest lines stay.
 */
public object Breadcrumbs {
    /** Enough to see the way to a problem, small enough to read in a report. */
    public const val MAX_STEPS: Int = 50

    private val steps = ArrayDeque<Step>()
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

    /** Keeps [step] (`Editor: OnSaveShortcut`) with the time, dropping the oldest once there are [MAX_STEPS]. */
    public fun record(step: String) {
        val time = LocalTime.now().format(timeFormat)
        synchronized(steps) {
            val last = steps.lastOrNull()
            if (last?.name == step) {
                steps[steps.lastIndex] = last.copy(time = time, count = last.count + 1)
            } else {
                steps.addLast(Step(time = time, name = step, count = 1))
            }
            while (steps.size > MAX_STEPS) steps.removeFirst()
        }
    }

    /** The steps kept, oldest first. */
    public fun recent(): List<String> = synchronized(steps) {
        steps.map { "${it.time} ${it.name}" + if (it.count > 1) " (×${it.count})" else "" }
    }

    public fun clear() {
        synchronized(steps) { steps.clear() }
    }

    private data class Step(
        val time: String,
        val name: String,
        val count: Int
    )
}
