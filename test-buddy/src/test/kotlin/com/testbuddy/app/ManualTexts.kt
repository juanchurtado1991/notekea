package com.testbuddy.app

import com.testbuddy.ManualCaseBuilder

/** Fills a manual case's three texts for tests that only care about its title. */
internal fun ManualCaseBuilder.texts(title: String) {
    title(text = title)
    steps(text = "Do it")
    expected(text = "It works")
}
