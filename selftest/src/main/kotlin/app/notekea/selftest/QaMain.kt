package app.notekea.selftest

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.di.desktopModules
import app.notekea.runNotekea
import com.testbuddy.only
import com.testbuddy.testBuddyMain
import org.koin.dsl.module

private const val CASES_PROPERTY = "notekea.selftest.cases"

/**
 * Notekea QA (`qa/SELF_TEST.md`): Notekea itself, with its data in a folder of its own ([QaAppData]) so the tester's
 * Notekea is never touched, and the Test Buddy manager next to its window. `./gradlew :selftest:run -Pcases=1.6`
 * opens it with only the cases of 1.6, for a feature's smoke test.
 */
fun main(args: Array<String>) {
    val prefixes = System.getProperty(CASES_PROPERTY).orEmpty().split(",").map(String::trim).filter(String::isNotEmpty)
    testBuddyMain(args = args, suite = NotekeaSuite.suite.only(prefixes = prefixes)) { manager ->
        runNotekea(modules = desktopModules + qaModule(), extraWindows = manager)
    }
}

private fun qaModule() = module {
    single<PlatformConventions> {
        get<ResolvePlatformConventionsUseCase>().invoke().let {
            it.copy(appDataDirectory = QaAppData.of(it).toString())
        }
    }
}
