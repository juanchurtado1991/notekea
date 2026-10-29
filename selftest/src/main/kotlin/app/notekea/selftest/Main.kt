package app.notekea.selftest

import com.testbuddy.only
import com.testbuddy.testBuddyApp

private const val CASES_PROPERTY = "notekea.selftest.cases"

/**
 * The "Notekea Self-Test" app testers install (`qa/SELF_TEST.md`); `./gradlew :selftest:run -Pcases=1.6` opens it with
 * only the cases of 1.6, for a feature's smoke test.
 */
fun main() {
    val prefixes = System.getProperty(CASES_PROPERTY).orEmpty().split(",").map(String::trim).filter(String::isNotEmpty)
    testBuddyApp(suite = NotekeaSuite.suite.only(prefixes = prefixes))
}
