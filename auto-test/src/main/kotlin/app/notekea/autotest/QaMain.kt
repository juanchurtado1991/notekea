package app.notekea.autotest

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.di.desktopModules
import app.notekea.runNotekea
import com.testbuddy.testBuddyMain
import org.koin.dsl.module

/**
 * Notekea QA (`qa/AUTO_TEST.md`): Notekea itself, with its data in a folder of its own ([QaAppData]) so the tester's
 * Notekea is never touched, and the Test Buddy manager next to its window. `./gradlew :auto-test:run -Pcases=1.6`
 * opens it with only the cases of 1.6, for a feature's smoke test.
 */
fun main(args: Array<String>) {
    QaLanguage.startInSpanishOnce(folder = QaAppData.current())

    testBuddyMain(
        args = args,
        suite = NotekeaSuite.selected()
    ) { manager ->
        runNotekea(
            modules = desktopModules + qaModule(),
            extraWindows = manager
        )
    }
}

private fun qaModule() = module {
    single<PlatformConventions> {
        get<ResolvePlatformConventionsUseCase>().invoke().let {
            it.copy(appDataDirectory = QaAppData.of(it).toString())
        }
    }
}
