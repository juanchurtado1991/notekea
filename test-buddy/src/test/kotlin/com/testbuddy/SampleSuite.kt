package com.testbuddy

import java.nio.file.Path
import java.nio.file.Paths

/** A suite of the [SampleApp] with the given [cases], on the test data `sample-data`. */
internal fun sampleSuite(cases: SuiteBuilder.() -> Unit): Suite = testBuddy(appName = "Sample", version = "1.0") {
    launch { folders -> SampleApp(folders = folders) }
    data(from = sampleData())
    window(width = 640, height = 480)
    cases()
}

internal fun sampleData(): Path = Paths.get(requireNotNull(Suite::class.java.getResource("/sample-data")).toURI())
